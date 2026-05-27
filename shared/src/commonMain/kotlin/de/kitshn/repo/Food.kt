package de.kitshn.repo

import co.touchlab.kermit.Logger
import de.kitshn.AppDatabase
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.api.tandoor.model.TandoorFood
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntryFood
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarketCategory
import de.kitshn.db.entity.FoodEntity
import de.kitshn.db.entity.FoodPendingDeleteEntity
import de.kitshn.db.entity.FoodWithRelations
import de.kitshn.db.entity.toEntity
import de.kitshn.db.entity.toFood
import de.kitshn.db.entity.toMinimalModel
import de.kitshn.db.entity.toModel
import de.kitshn.session.TandoorSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private const val MEMORY_THRESHOLD = 2000
private const val MANY_THRESHOLD = 500
private const val SEARCH_DEBOUNCE_MS = 300L

@OptIn(kotlinx.coroutines.FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FoodRepo(
    db: AppDatabase,
    private val supermarketCategoryRepo: SupermarketCategoryRepo,
    private val unitRepo: UnitRepo,
    private val session: TandoorSession,
    private val scope: CoroutineScope,
    periodicInterval: Duration? = null,
) : SyncableRepo(db.repoMetaDao(), periodicInterval) {
    override val repoMetaName: String = "food"
    override val reconcileCapabilities = setOf(ReconcileCapability.Bucketed)

    private val dao = db.foodDao()

    // there can be many foods in -> fallback to just db just in case
    private val cache = MutableStateFlow<List<FoodWithRelations>?>(null)
    private val remoteIdByLocalIdCache = MutableStateFlow<Map<Int, Int?>>(emptyMap())

    private val searchQuery = MutableStateFlow("")

    init {
        scope.launch { observeLocalChanges() }
        scope.launch { hydrateFromSearchQueries() }
    }

    fun observe(): Flow<List<TandoorFood>> =
        if (cache.value != null)
            cache.map { it?.map(FoodWithRelations::toModel) ?: emptyList() }
        else
            dao.getAllWithRelations().map { it.map(FoodWithRelations::toModel) }

    fun search(query: String): Flow<List<TandoorFood>> {
        if (query.isBlank()) return flowOf(emptyList())
        searchQuery.value = query
        return if (cache.value != null)
            cache.map {
                it?.let { rankSearch(it, query).map(FoodWithRelations::toModel) } ?: emptyList()
            }
        else
            dao.searchWithRelations(query).map { it.map(FoodWithRelations::toModel) }
    }

    private suspend fun hydrateFromSearchQueries() {
        searchQuery
            .debounce(SEARCH_DEBOUNCE_MS.milliseconds)
            .distinctUntilChanged()
            .filter { it.length >= 2 }
            .collectLatest { query ->
                val client = session.client ?: return@collectLatest
                runCatching { upsertAll(client.food.list(query = query, pageSize = 20).results) }
            }
    }

    private fun rankSearch(
        foods: List<FoodWithRelations>,
        query: String,
    ): List<FoodWithRelations> {
        val q = query.lowercase()
        return foods
            .mapNotNull { row ->
                val name = row.food.name.lowercase()
                val plural = row.food.plural_name?.lowercase() ?: ""
                val rank = when {
                    name == q || plural == q -> 0
                    name.startsWith(q) || plural.startsWith(q) -> 1
                    name.contains(q) || plural.contains(q) -> 2
                    else -> return@mapNotNull null
                }
                rank to row
            }
            .sortedWith(
                compareBy(
                    { it.first },
                    { it.second.food.name.length },
                    { it.second.food.name })
            )
            .map { it.second }
    }

    suspend fun findOrCreate(name: String): TandoorFood {
        val q = name.lowercase()
        return mutex.withLock {
            dao.findByName(q)?.let { existing ->
                return@withLock retrieve(existing.localId) ?: existing.toMinimalModel()
            }
            val server = session.client?.runCatching { food.create(name) }?.getOrNull()
            val entity = if (server == null) {
                FoodEntity(name = name)
            } else {
                supermarketCategoryRepo.upsertAll(listOfNotNull(server.supermarket_category))
                unitRepo.upsertAll(listOfNotNull(server.properties_food_unit))
                markItemSynced(server.id)
                server.toEntity(
                    unitLocalId = server.properties_food_unit?.id?.let {
                        unitRepo.localIdByRemoteId(
                            it
                        )
                    },
                    categoryLocalId = server.supermarket_category?.id?.let {
                        supermarketCategoryRepo.localIdByRemoteId(
                            it
                        )
                    },
                )
            }
            val localId = dao.insert(entity).toInt()
            retrieve(localId) ?: entity.copy(localId = localId).toMinimalModel()
        }
    }

    private suspend fun retrieve(localId: Int): TandoorFood? =
        dao.getWithRelationsByLocalId(localId)?.toModel()

    suspend fun delete(localId: Int): DeleteResult {
        val row = dao.findByLocalId(localId) ?: return DeleteResult.NotFound
        val remoteId = row.remoteId
        if (remoteId == null) {
            dao.deleteByLocalId(localId)
            return DeleteResult.Deleted
        }
        val client = session.client
        if (client != null && session.isOnline.value) {
            try {
                client.food.delete(remoteId)
                dao.deleteByLocalId(localId)
                return DeleteResult.Deleted
            } catch (e: TandoorRequestsError) {
                if (e.isNetworkFailure) return tombstoneAndDelete(remoteId, row.name, localId)
                Logger.w(e, tag = repoTag) { "Server refused to delete food (remoteId=$remoteId)" }
                return DeleteResult.InUse
            }
        }
        return tombstoneAndDelete(remoteId, row.name, localId)
    }

    private suspend fun tombstoneAndDelete(
        remoteId: Int,
        name: String,
        localId: Int,
    ): DeleteResult {
        dao.insertPendingDelete(FoodPendingDeleteEntity(remoteId, name))
        dao.deleteByLocalId(localId)
        return DeleteResult.Deleted
    }

    suspend fun remoteIdByLocalId(localId: Int): Int? {
        val cache = remoteIdByLocalIdCache.value
        if (localId in cache) return cache[localId]
        return dao.remoteIdByLocalId(localId)
    }

    suspend fun toRemote(food: TandoorFood): TandoorFood? {
        val remoteId = remoteIdByLocalId(food.id) ?: return null
        val unit = food.properties_food_unit?.let { unitRepo.toRemote(it) ?: return null }
        val category =
            food.supermarket_category?.let { supermarketCategoryRepo.toRemote(it) ?: return null }
        return food.copy(
            id = remoteId,
            properties_food_unit = unit,
            supermarket_category = category,
        )
    }

    suspend fun updateSupermarketCategory(
        id: Int,
        category: TandoorSupermarketCategory?,
    ): TandoorFood? {
        val client = session.client ?: return null
        val food = retrieve(id) ?: return null
        val remoteFood = toRemote(food) ?: return null
        val remoteCategory = category?.let { supermarketCategoryRepo.toRemote(it) ?: return null }
        return try {
            val updated = client.food.updateSupermarketCategory(remoteFood, remoteCategory)
            upsertAll(listOf(updated))
            retrieve(id) ?: updated
        } catch (e: TandoorRequestsError) {
            Logger.w(
                e,
                tag = repoTag
            ) { "Failed to update supermarket category for food (remoteId=${remoteFood.id}" }
            null
        }
    }

    suspend fun remoteIdByName(name: String): Int? = dao.remoteIdByName(name.lowercase())

    suspend fun localIdByRemoteId(remoteId: Int): Int? = dao.localIdByRemoteId(remoteId)

    override suspend fun syncPendingBefore() {
        syncPendingDeletes()
        syncPendingCreates()
    }

    private suspend fun syncPendingDeletes() {
        val client = session.client ?: return
        for (tombstone in dao.getPendingDeletes()) {
            try {
                client.food.delete(tombstone.remoteId)
                dao.deletePendingDelete(tombstone.remoteId)
            } catch (e: TandoorRequestsError) {
                if (e.isNetworkFailure) continue
                Logger.w(e, tag = repoTag) {
                    "Server refused food delete Food (remoteId=${tombstone.remoteId}). Clearing tombstone..."
                }
                dao.deletePendingDelete(tombstone.remoteId)
            }
        }
    }

    private suspend fun syncPendingCreates() {
        val client = session.client ?: return
        for (stub in dao.getPendingCreates()) {
            val server = try {
                client.food.create(stub.name)
            } catch (e: TandoorRequestsError) {
                Logger.w(e, tag = repoTag) { "Failed pending create for localId=${stub.localId}" }
                continue
            }
            mutex.withLock {
                supermarketCategoryRepo.upsertAll(listOfNotNull(server.supermarket_category))
                unitRepo.upsertAll(listOfNotNull(server.properties_food_unit))
                val current = dao.findByLocalId(stub.localId) ?: return@withLock
                dao.update(
                    server.toEntity(
                        localId = current.localId,
                        unitLocalId = server.properties_food_unit?.id?.let {
                            unitRepo.localIdByRemoteId(
                                it
                            )
                        },
                        categoryLocalId = server.supermarket_category?.id?.let {
                            supermarketCategoryRepo.localIdByRemoteId(
                                it
                            )
                        },
                    )
                )
                markItemSynced(server.id)
            }
        }
    }

    override suspend fun performFullReconcile(): ReconcileResult? {
        val client = session.client ?: return null
        val tombstoned = dao.getPendingDeleteRemoteIds().toSet()
        val resp = client.food.listAll { foods ->
            upsertAll(foods.filter { it.id !in tombstoned })
            false
        }
        val keepIds = resp.results.map { it.id }.filter { it !in tombstoned }
        dao.deleteSyncedNotIn(keepIds)
        return ReconcileResult(nextPage = 1)
    }

    override suspend fun performBucketReconcile(
        strategy: ReconcileStrategy.Bucketed,
        cursorPage: Int,
    ): ReconcileResult? {
        val client = session.client ?: return null
        val tombstoned = dao.getPendingDeleteRemoteIds().toSet()
        val resp = client.food.list(page = cursorPage, pageSize = strategy.pageSize)
        upsertAll(resp.results.filter { it.id !in tombstoned })
        return ReconcileResult(nextPage = if (resp.next == null) 1 else cursorPage + 1)
    }


    suspend fun syncItem(remoteId: Int, force: Boolean = false) {
        if (!force && withinItemSyncInterval(remoteId)) return
        val client = session.client ?: return
        try {
            upsertAll(listOf(client.food.get(remoteId)))
        } catch (e: TandoorRequestsError) {
            Logger.e(e, tag = repoTag) { "Failed to sync food remoteId=$remoteId" }
        }
    }

    private suspend fun observeLocalChanges() {
        dao.getAllWithRelations().collect { rows ->
            cache.value = rows.takeIf { it.size < MEMORY_THRESHOLD }
            remoteIdByLocalIdCache.value = rows.associate { it.food.localId to it.food.remoteId }
            if (rows.size > MANY_THRESHOLD) datasetSize = DatasetSize.MANY
        }
    }

    internal suspend fun upsertShoppingListEntryFoods(foods: List<TandoorShoppingListEntryFood>) {
        upsertAll(foods.map { it.toFood() })
    }

    internal suspend fun upsertAll(foods: List<TandoorFood>) {
        if (foods.isEmpty()) return

        val categories = foods.mapNotNull { it.supermarket_category }
        if (categories.isNotEmpty()) supermarketCategoryRepo.upsertAll(categories)

        val units = foods.mapNotNull { it.properties_food_unit }
        if (units.isNotEmpty()) unitRepo.upsertAll(units)

        for (food in foods) {
            val unitLocalId = food.properties_food_unit?.id?.let { unitRepo.localIdByRemoteId(it) }
            val categoryLocalId = food.supermarket_category?.id?.let {
                supermarketCategoryRepo.localIdByRemoteId(it)
            }
            dao.upsertByRemoteId(
                food.toEntity(
                    unitLocalId = unitLocalId,
                    categoryLocalId = categoryLocalId,
                )
            )
            markItemSynced(food.id)
        }
    }

}
