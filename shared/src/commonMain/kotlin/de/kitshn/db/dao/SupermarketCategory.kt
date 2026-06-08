package de.kitshn.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import de.kitshn.db.entity.SupermarketCategoryEntity
import de.kitshn.db.entity.SupermarketCategoryPendingDeleteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupermarketCategoryDao {
    @Upsert
    suspend fun upsertAll(categories: List<SupermarketCategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIfAbsent(categories: List<SupermarketCategoryEntity>)

    @Insert
    suspend fun insert(entity: SupermarketCategoryEntity): Long

    @Update
    suspend fun update(entity: SupermarketCategoryEntity)

    @Query("SELECT * FROM supermarket_category ORDER BY name ASC")
    fun getAllAsFlow(): Flow<List<SupermarketCategoryEntity>>

    @Query("SELECT * FROM supermarket_category WHERE remoteId = :remoteId LIMIT 1")
    suspend fun findByRemoteId(remoteId: Int): SupermarketCategoryEntity?

    @Query("SELECT * FROM supermarket_category WHERE id = :localId LIMIT 1")
    suspend fun findByLocalId(localId: Int): SupermarketCategoryEntity?

    @Query("SELECT * FROM supermarket_category WHERE LOWER(name) = :lowercaseName LIMIT 1")
    suspend fun findByName(lowercaseName: String): SupermarketCategoryEntity?

    @Query("SELECT id FROM supermarket_category WHERE remoteId = :remoteId LIMIT 1")
    suspend fun localIdByRemoteId(remoteId: Int): Int?

    @Query("SELECT remoteId FROM supermarket_category WHERE id = :localId LIMIT 1")
    suspend fun remoteIdByLocalId(localId: Int): Int?

    @Query("DELETE FROM supermarket_category WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)

    @Query("DELETE FROM supermarket_category WHERE remoteId IS NOT NULL AND remoteId NOT IN (:serverIds)")
    suspend fun deleteSyncedNotIn(serverIds: List<Int>)

    @Transaction
    suspend fun upsertByRemoteId(entity: SupermarketCategoryEntity): Int {
        val remoteId = requireNotNull(entity.remoteId) {
            "upsertByRemoteId requires a non-null remoteId"
        }
        val existingId = localIdByRemoteId(remoteId)
        return if (existingId != null) {
            update(entity.copy(localId = existingId))
            existingId
        } else {
            insert(entity).toInt()
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingDelete(tombstone: SupermarketCategoryPendingDeleteEntity)

    @Query("SELECT * FROM supermarket_category_pending_delete")
    suspend fun getPendingDeletes(): List<SupermarketCategoryPendingDeleteEntity>

    @Query("SELECT remoteId FROM supermarket_category_pending_delete")
    suspend fun getPendingDeleteRemoteIds(): List<Int>

    @Query("DELETE FROM supermarket_category_pending_delete WHERE remoteId = :remoteId")
    suspend fun deletePendingDelete(remoteId: Int)

    @Query("DELETE FROM supermarket_category")
    suspend fun deleteAll()

    @Query("DELETE FROM supermarket_category_pending_delete")
    suspend fun deleteAllPendingDeletes()
}
