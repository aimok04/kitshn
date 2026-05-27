package de.kitshn.repo

import co.touchlab.kermit.Logger
import de.kitshn.AppDatabase
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.api.tandoor.model.TandoorUnit
import de.kitshn.db.entity.UnitEntity
import de.kitshn.db.entity.UnitPendingDeleteEntity
import de.kitshn.db.entity.toEntity
import de.kitshn.db.entity.toModel
import de.kitshn.session.TandoorSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

class UnitRepo(
    db: AppDatabase,
    private val session: TandoorSession,
    private val scope: CoroutineScope,
    periodicInterval: Duration? = null,
) : SyncableRepo(db.repoMetaDao(), periodicInterval, reconcileInterval = 7.days) {
    override val repoMetaName: String = "unit"
    override val reconcileCapabilities = emptySet<ReconcileCapability>()

    private val _units = MutableStateFlow<List<TandoorUnit>>(emptyList())
    val units = _units.asStateFlow()

    private val dao = db.unitDao()

    init {
        datasetSize = DatasetSize.FEW

        scope.launch {
            dao.getAllAsFlow().collect { entities ->
                _units.value = entities.map { it.toModel() }
            }
        }
    }

    fun search(query: String): Flow<List<TandoorUnit>> {
        if (query.isBlank()) return flowOf(emptyList())
        return _units.map { rankSearch(it, query) }
    }

    suspend fun localIdByRemoteId(remoteId: Int): Int? = dao.localIdByRemoteId(remoteId)

    suspend fun remoteIdByLocalId(localId: Int): Int? = dao.remoteIdByLocalId(localId)

    suspend fun remoteIdByName(name: String): Int? = dao.remoteIdByName(name.lowercase())

    suspend fun toRemote(unit: TandoorUnit): TandoorUnit? {
        val localId = unit.id
        val remoteId = remoteIdByLocalId(localId) ?: return null
        return unit.copy(id = remoteId)
    }

    suspend fun toLocal(unit: TandoorUnit): TandoorUnit? {
        val remoteId = unit.id
        val localId = localIdByRemoteId(remoteId) ?: return null
        return unit.copy(id = localId)
    }

    suspend fun findOrCreate(name: String): TandoorUnit {
        dao.findByName(name.lowercase())?.let { return it.toModel() }
        val localId = dao.insert(UnitEntity(name = name)).toInt()
        use(localId, scope)
        return dao.findByLocalId(localId)?.toModel()
            ?: UnitEntity(localId = localId, name = name).toModel()
    }

    suspend fun delete(localId: Int): DeleteResult {
        val row = dao.findByLocalId(localId) ?: return DeleteResult.NotFound
        // Locals are protected by FK restrictions
        val remoteId = row.remoteId ?: return tryLocalDelete(localId)

        val client = session.client
        if (client != null && session.isOnline.value) {
            try {
                client.unit.delete(remoteId)
            } catch (e: TandoorRequestsError) {
                // sync to remote but offline currently
                if (e.isNetworkFailure) return tombstoneAndDelete(remoteId, row.name, localId)
                Logger.e(e, tag = repoTag) { "Server refused unit delete remoteId=$remoteId" }
                return DeleteResult.InUse
            }
            return tryLocalDelete(localId)
        }
        return tombstoneAndDelete(remoteId, row.name, localId)
    }

    private suspend fun tryLocalDelete(localId: Int): DeleteResult = try {
        dao.deleteByLocalId(localId)
        DeleteResult.Deleted
    } catch (e: Throwable) {
        // Only realistic failure on this statement is the RESTRICT FK firing.
        Logger.w(e, tag = repoTag) { "Local FK refused unit delete localId=$localId" }
        DeleteResult.InUse
    }

    private suspend fun tombstoneAndDelete(
        remoteId: Int,
        name: String,
        localId: Int,
    ): DeleteResult {
        dao.insertPendingDelete(UnitPendingDeleteEntity(remoteId, name))
        return tryLocalDelete(localId).also {
            if (it != DeleteResult.Deleted) dao.deletePendingDelete(remoteId)
        }
    }

    override suspend fun syncPendingBefore() {
        syncPendingDeletes()
        syncPendingCreates()
    }

    override suspend fun performFullReconcile(): ReconcileResult? {
        val client = session.client ?: return null
        return try {
            val tombstoned = dao.getPendingDeleteRemoteIds().toSet()
            val resp = client.unit.listAll { batch ->
                upsertAll(batch.filter { it.id !in tombstoned })
                false
            }
            val keepIds = resp.results.map { it.id }.filter { it !in tombstoned }
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
                val resp = client.unit.create(name)
                dao.upsertAll(listOf(resp.toEntity(localId = localId)))
                markItemSynced(resp.id)
            } else {
                val resp = client.unit.retrieve(remoteId)
                upsertAll(listOf(resp))
            }
        } catch (e: TandoorRequestsError) {
            Logger.w(e, tag = repoTag) { "Failed item sync localId=$localId" }
        }
    }

    internal suspend fun upsertAll(units: List<TandoorUnit>) {
        for (unit in units) {
            dao.upsertByRemoteId(unit.toEntity())
            markItemSynced(unit.id)
        }
    }

    private suspend fun syncPendingDeletes() {
        val client = session.client ?: return
        for (tombstone in dao.getPendingDeletes()) {
            try {
                client.unit.delete(tombstone.remoteId)
                dao.deletePendingDelete(tombstone.remoteId)
            } catch (e: TandoorRequestsError) {
                if (e.isNetworkFailure) continue
                Logger.w(e, tag = repoTag) {
                    "Server refused unit delete remoteId=${tombstone.remoteId}; clearing tombstone"
                }
                dao.deletePendingDelete(tombstone.remoteId)
            }
        }
    }

    private suspend fun syncPendingCreates() {
        val client = session.client ?: return
        for (stub in dao.getPendingCreates()) {
            val server = try {
                client.unit.create(stub.name)
            } catch (e: TandoorRequestsError) {
                Logger.w(e, tag = repoTag) { "Failed pending create for localId=${stub.localId}" }
                continue
            }
            dao.upsertAll(listOf(server.toEntity(localId = stub.localId)))
            markItemSynced(server.id)
        }
    }

    private fun rankSearch(
        units: List<TandoorUnit>,
        query: String,
    ): List<TandoorUnit> {
        val q = query.lowercase()
        return units
            .mapNotNull { unit ->
                val name = unit.name.lowercase()
                val plural = unit.plural_name?.lowercase() ?: ""
                val rank = when {
                    name == q || plural == q -> 0
                    name.startsWith(q) || plural.startsWith(q) -> 1
                    name.contains(q) || plural.contains(q) -> 2
                    else -> return@mapNotNull null
                }
                rank to unit
            }
            .sortedWith(compareBy({ it.first }, { it.second.name.length }, { it.second.name }))
            .map { it.second }
    }
}
