package de.kitshn.repo

import co.touchlab.kermit.Logger
import de.kitshn.AppDatabase
import de.kitshn.api.tandoor.model.TandoorFood
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntryFood
import de.kitshn.db.entity.FoodEntity
import de.kitshn.db.entity.FoodWithRelations
import de.kitshn.db.entity.toEntity
import de.kitshn.db.entity.toMinimalEntity
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(kotlinx.coroutines.FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FoodRepo(
    db: AppDatabase,
    private val supermarketCategoryRepo: SupermarketCategoryRepo,
    private val unitRepo: UnitRepo,
    private val session: TandoorSession,
    private val scope: CoroutineScope,
    periodicInterval: Duration? = null,
) : SyncableRepo(periodicInterval) {
    private val dao = db.foodDao()

    private val searchQuery = MutableStateFlow("")

    // Cache for high-quality ranking. If null, we use DB-backed search
    private val cache = MutableStateFlow<List<FoodWithRelations>?>(null)
    private val memoryThreshold = 2000

    init {
        scope.launch {
            // flow keeps the cache up to date and also the search results
            dao.getAllWithRelations().collect {
                if (it.size < memoryThreshold) {
                    cache.value = it
                } else {
                    // Dataset grew too large, drop cache to save memory
                    cache.value = null
                }
            }
        }

        // Trigger remote augmentation whenever search query changes (debounced)
        scope.launch {
            searchQuery
                .debounce(300.milliseconds)
                .distinctUntilChanged()
                .filter { it.length >= 2 }
                .collectLatest { query ->
                    augmentFromRemote(query)
                }
        }
    }

    private suspend fun augmentFromRemote(query: String) {
        val client = session.client ?: return
        try {
            val response = client.food.list(query = query, pageSize = 20)
            supermarketCategoryRepo.upsertAll(response.results.mapNotNull { it.supermarket_category })
            unitRepo.upsertAll(response.results.mapNotNull { it.properties_food_unit })
            upsertAll(response.results)
        } catch (_: Exception) {
            // do nothing and be happy
        }
    }

    fun observe(): Flow<List<TandoorFood>> {
        val cached = cache.value
        if (cached != null) return cache.map { it?.map(FoodWithRelations::toModel) ?: emptyList() }

        return dao
            .getAllWithRelations()
            .map { it.map(FoodWithRelations::toModel) }
    }

    override suspend fun performSync() {
        val client = session.client ?: return
        try {
            client.food.retrieve { foods ->
                supermarketCategoryRepo.upsertAll(foods.mapNotNull { it.supermarket_category })
                unitRepo.upsertAll(foods.mapNotNull { it.properties_food_unit })
                upsertAll(foods)
            }
        } catch (e: Exception) {
            Logger.e(e, tag = "FoodRepo") { "Failed to sync foods" }
        }
    }

    override suspend fun performReconcile() {
        val client = session.client ?: return
        try {
            val remoteIds =
                client.food
                    .retrieve()
                    .results
                    .map { it.id }
            if (remoteIds.isNotEmpty()) {
                dao.deleteAllExcept(remoteIds)
            }
        } catch (e: Exception) {
            Logger.e(e, tag = "FoodRepo") { "Failed to reconcile foods" }
        }
    }

    suspend fun upsertAll(foods: List<TandoorFood>) {
        if (foods.isEmpty()) return
        dao.upsertAll(foods.map { it.toEntity() })
    }

    fun findIdByName(name: String): Int? {
        val q = name.lowercase()
        return cache.value
            ?.firstOrNull { it.food.name.lowercase() == q }
            ?.food
            ?.id
    }

    // Partial entries do not overwrite existing entries.
    suspend fun insertPartial(foods: List<TandoorShoppingListEntryFood>) {
        if (foods.isEmpty()) return
        dao.insertAllIfAbsent(foods.map { it.toMinimalEntity() })
    }

    suspend fun insertStub(name: String): Int {
        val localId = dao.nextLocalId()
        dao.insertAllIfAbsent(listOf(FoodEntity(id = localId, name = name)))
        return localId
    }

    suspend fun deleteById(id: Int) = dao.delete(id)

    fun search(query: String): Flow<List<TandoorFood>> {
        if (query.isBlank()) return flowOf(emptyList())

        // Update the search query to trigger remote augmentation in the background
        searchQuery.value = query

        return if (cache.value != null) {
            // High-quality in-memory ranking for regular users.
            // Observing 'cache' makes this reactive to background augmentation.
            cache.map { it?.let { rankSearch(it, query).map(FoodWithRelations::toModel) } ?: emptyList() }
        } else {
            // Memory-efficient DB search for too large datasets.
            // Room Flows are reactive by default.
            dao
                .searchWithRelations(query)
                .map { it.map(FoodWithRelations::toModel) }
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
                val rank =
                    when {
                        name == q || plural == q -> 0
                        name.startsWith(q) || plural.startsWith(q) -> 1
                        name.contains(q) || plural.contains(q) -> 2
                        else -> return@mapNotNull null
                    }
                rank to row
            }.sortedWith(compareBy({ it.first }, { it.second.food.name.length }, { it.second.food.name }))
            .map { it.second }
    }
}
