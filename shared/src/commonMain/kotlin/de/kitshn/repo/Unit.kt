package de.kitshn.repo

import co.touchlab.kermit.Logger
import de.kitshn.AppDatabase
import de.kitshn.api.tandoor.model.TandoorUnit
import de.kitshn.db.entity.UnitEntity
import de.kitshn.db.entity.toEntity
import de.kitshn.db.entity.toModel
import de.kitshn.session.TandoorSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration

class UnitRepo(
    db: AppDatabase,
    private val session: TandoorSession,
    scope: CoroutineScope,
    periodicInterval: Duration? = null,
) : SyncableRepo(periodicInterval) {
    private val dao = db.unitDao()
    private val cache = MutableStateFlow<List<UnitEntity>>(emptyList())

    init {
        scope.launch { dao.getAll().collect { cache.value = it } }
    }

    fun observe(): Flow<List<TandoorUnit>> = cache.map { it.map(UnitEntity::toModel) }

    override suspend fun performSync() {
        val client = session.client ?: return
        try {
            client.unit.retrieve {
                upsertAll(it)
            }
        } catch (e: Exception) {
            Logger.e(e, tag = "UnitRepo") { "Failed to sync units" }
        }
    }

    override suspend fun performReconcile() {
        val client = session.client ?: return
        try {
            val remoteIds =
                client.unit
                    .retrieve()
                    .results
                    .map { it.id }
            if (remoteIds.isNotEmpty()) {
                dao.deleteAllExcept(remoteIds)
            }
        } catch (e: Exception) {
            Logger.e(e, tag = "UnitRepo") { "Failed to reconcile units" }
        }
    }

    suspend fun upsertAll(units: List<TandoorUnit>) {
        if (units.isEmpty()) return
        dao.upsertAll(units.map { it.toEntity() })
    }

    fun findIdByName(name: String): Int? {
        val q = name.lowercase()
        return cache.value.firstOrNull { it.name.lowercase() == q }?.id
    }

    suspend fun insertStub(name: String): Int {
        val localId = dao.nextLocalId()
        dao.upsertAll(listOf(UnitEntity(id = localId, name = name)))
        return localId
    }

    suspend fun deleteById(id: Int) = dao.delete(id)

    fun search(query: String): Flow<List<TandoorUnit>> {
        if (query.isBlank()) return flowOf(emptyList())
        return cache.map { rankSearch(it, query).map(UnitEntity::toModel) }
    }

    private fun rankSearch(
        units: List<UnitEntity>,
        query: String,
    ): List<UnitEntity> {
        val q = query.lowercase()
        return units
            .mapNotNull { unit ->
                val name = unit.name.lowercase()
                val plural = unit.plural_name?.lowercase() ?: ""
                val rank =
                    when {
                        name == q || plural == q -> 0
                        name.startsWith(q) || plural.startsWith(q) -> 1
                        name.contains(q) || plural.contains(q) -> 2
                        else -> return@mapNotNull null
                    }
                rank to unit
            }.sortedWith(compareBy({ it.first }, { it.second.name.length }, { it.second.name }))
            .map { it.second }
    }
}
