package de.kitshn.repo

import co.touchlab.kermit.Logger
import de.kitshn.AppDatabase
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarketCategory
import de.kitshn.db.entity.SupermarketCategoryEntity
import de.kitshn.db.entity.SupermarketCategoryPendingDeleteEntity
import de.kitshn.db.entity.toEntity
import de.kitshn.db.entity.toModel
import de.kitshn.session.TandoorSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

class SupermarketCategoryRepo(
    db: AppDatabase,
    private val session: TandoorSession,
    private val scope: CoroutineScope,
    periodicInterval: Duration? = null,
) : SyncableRepo(db.repoMetaDao(), periodicInterval, reconcileInterval = 7.days) {
    override val repoMetaName: String = "supermarket_category"
    override val reconcileCapabilities = emptySet<ReconcileCapability>()

    private val _categories = MutableStateFlow<List<TandoorSupermarketCategory>>(emptyList())
    val categories = _categories.asStateFlow()

    private val dao = db.supermarketCategoryDao()

    init {
        datasetSize = DatasetSize.FEW

        scope.launch {
            dao.getAllAsFlow().collect { entity ->
                _categories.value = entity.map { it.toModel() }
            }
        }
    }

    suspend fun localIdByRemoteId(remoteId: Int): Int? = dao.localIdByRemoteId(remoteId)

    suspend fun remoteIdByLocalId(localId: Int): Int? = dao.remoteIdByLocalId(localId)

    suspend fun toRemote(category: TandoorSupermarketCategory): TandoorSupermarketCategory? {
        val localId = category.id ?: return category
        val remoteId = remoteIdByLocalId(localId) ?: return null
        return category.copy(id = remoteId)
    }

    suspend fun toLocal(category: TandoorSupermarketCategory): TandoorSupermarketCategory? {
        val remoteId = category.id ?: return category
        val localId = localIdByRemoteId(remoteId) ?: return null
        return category.copy(id = localId)
    }

    suspend fun create(name: String): TandoorSupermarketCategory {
        val localId = dao.findOrInsert(SupermarketCategoryEntity(name = name))
        use(localId, scope)
        return dao.findByLocalId(localId)?.toModel()
            ?: SupermarketCategoryEntity(localId = localId, name = name).toModel()
    }

    suspend fun delete(localId: Int) {
        val row = dao.findByLocalId(localId) ?: return
        val remoteId = row.remoteId
        if (remoteId == null) {
            // offline-only no need for tombstone
            dao.deleteByLocalId(localId)
            return
        }
        val client = session.client
        if (client != null && session.isOnline.value) {
            try {
                client.supermarket.deleteCategory(remoteId)
                dao.deleteByLocalId(localId)
                return
            } catch (e: TandoorRequestsError) {
                if (!e.isNetworkFailure) {
                    Logger.w(e, tag = repoTag) {
                        "Server refused category delete remoteId=$remoteId; dropping local row"
                    }
                    dao.deleteByLocalId(localId)
                    return
                }
            }
        }
        //tombstone it but still delete from all referee's
        dao.insertPendingDelete(SupermarketCategoryPendingDeleteEntity(remoteId, row.name))
        dao.deleteByLocalId(localId)
    }

    override suspend fun syncPendingBefore() {
        val client = session.client ?: return
        for (tombstone in dao.getPendingDeletes()) {
            try {
                client.supermarket.deleteCategory(tombstone.remoteId)
                dao.deletePendingDelete(tombstone.remoteId)
            } catch (e: TandoorRequestsError) {
                if (e.isNetworkFailure) continue
                Logger.w(e, tag = repoTag) {
                    "Server refused category delete remoteId=${tombstone.remoteId}; clearing tombstone"
                }
                dao.deletePendingDelete(tombstone.remoteId)
            }
        }
    }

    override suspend fun performFullReconcile(): ReconcileResult? {
        val client = session.client ?: return null
        return try {
            // normally the tombstone would be removed already in syncPendingBefore
            val tombstoned = dao.getPendingDeleteRemoteIds().toSet()
            val resp = client.supermarket.listAllCategories { categories ->
                upsertAll(categories.filter { it.id !in tombstoned })
                false
            }
            val keepIds = resp.results.mapNotNull { it.id }.filter { it !in tombstoned }
            dao.deleteSyncedNotIn(keepIds)
            ReconcileResult(nextPage = 1)
        } catch (e: TandoorRequestsError) {
            logSyncFailure(e, "reconcile")
            null
        }
    }

    override suspend fun syncItem(localId: Int) {
        val client = session.client ?: return
        val remoteId = remoteIdByLocalId(localId)

        try {
            if (remoteId == null) {
                val name = dao.findByLocalId(localId)?.name ?: return
                val resp = client.supermarket.createCategory(name)
                val newRemoteId = resp.id ?: return
                dao.upsertAll(listOf(resp.toEntity(localId = localId)))
                markItemSynced(newRemoteId)
            } else {
                val resp = client.supermarket.retrieveCategory(remoteId)
                upsertAll(listOf(resp))
            }
        } catch (e: TandoorRequestsError) {
            Logger.w(e, tag = repoTag) { "Failed item sync localId=$localId" }
        }
    }

    internal suspend fun upsertAll(categories: List<TandoorSupermarketCategory>) {
        for (category in categories) {
            val remoteId = category.id ?: continue
            dao.upsertByRemoteId(category.toEntity())
            markItemSynced(remoteId)
        }
    }
}
