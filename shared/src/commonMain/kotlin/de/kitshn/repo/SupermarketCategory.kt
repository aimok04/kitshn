package de.kitshn.repo

import co.touchlab.kermit.Logger
import de.kitshn.AppDatabase
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarketCategory
import de.kitshn.db.entity.SupermarketCategoryEntity
import de.kitshn.db.entity.toEntity
import de.kitshn.db.entity.toModel
import de.kitshn.session.TandoorSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration

class SupermarketCategoryRepo(
    db: AppDatabase,
    private val session: TandoorSession,
    scope: CoroutineScope,
    periodicInterval: Duration? = null,
) : SyncableRepo(periodicInterval) {
    private val dao = db.supermarketCategoryDao()

    fun observe(): Flow<List<TandoorSupermarketCategory>> = dao.getAll().map { it.map(SupermarketCategoryEntity::toModel) }

    override suspend fun performSync() {
        val client = session.client ?: return
        try {
            upsertAll(client.supermarket.fetchAllCategories())
        } catch (e: Exception) {
            Logger.e(e, tag = "SupermarketCategoryRepo") { "Failed to sync categories" }
        }
    }

    override suspend fun performReconcile() {
        val client = session.client ?: return
        try {
            val remoteIds = client.supermarket.fetchAllCategories().mapNotNull { it.id }
            if (remoteIds.isNotEmpty()) {
                dao.deleteAllExcept(remoteIds)
            }
        } catch (e: Exception) {
            Logger.e(e, tag = "SupermarketCategoryRepo") { "Failed to reconcile categories" }
        }
    }

    suspend fun upsertAll(categories: List<TandoorSupermarketCategory>) {
        if (categories.isEmpty()) return
        dao.upsertAll(categories.mapNotNull { it.toEntity() })
    }

    suspend fun insertAllIfAbsent(categories: List<TandoorSupermarketCategory>) {
        if (categories.isEmpty()) return
        dao.insertAllIfAbsent(categories.mapNotNull { it.toEntity() })
    }
}
