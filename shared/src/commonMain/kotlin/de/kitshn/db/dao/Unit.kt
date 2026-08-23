package de.kitshn.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import de.kitshn.db.entity.UnitEntity
import de.kitshn.db.entity.UnitPendingDeleteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitDao {
    @Upsert
    suspend fun upsertAll(units: List<UnitEntity>)

    @Insert
    suspend fun insert(entity: UnitEntity): Long

    @Transaction
    suspend fun findOrInsert(entity: UnitEntity): Int {
        findByName(entity.name.lowercase())?.let { return it.localId }
        return insert(entity).toInt()
    }

    @Update
    suspend fun update(entity: UnitEntity)

    @Query("SELECT * FROM unit ORDER BY name ASC")
    fun getAllAsFlow(): Flow<List<UnitEntity>>

    @Query("SELECT * FROM unit WHERE id = :localId LIMIT 1")
    suspend fun findByLocalId(localId: Int): UnitEntity?

    @Query("SELECT * FROM unit WHERE remoteId = :remoteId LIMIT 1")
    suspend fun findByRemoteId(remoteId: Int): UnitEntity?

    @Query("SELECT * FROM unit WHERE LOWER(name) = :lowercaseName LIMIT 1")
    suspend fun findByName(lowercaseName: String): UnitEntity?

    @Query("SELECT id FROM unit WHERE remoteId = :remoteId LIMIT 1")
    suspend fun localIdByRemoteId(remoteId: Int): Int?

    @Query("SELECT remoteId FROM unit WHERE id = :localId LIMIT 1")
    suspend fun remoteIdByLocalId(localId: Int): Int?

    @Query("SELECT remoteId FROM unit WHERE LOWER(name) = :lowercaseName LIMIT 1")
    suspend fun remoteIdByName(lowercaseName: String): Int?

    @Query("SELECT * FROM unit WHERE remoteId IS NULL ORDER BY id ASC")
    suspend fun getPendingCreates(): List<UnitEntity>

    @Query("DELETE FROM unit WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)

    @Query("DELETE FROM unit WHERE remoteId IS NOT NULL AND remoteId NOT IN (:serverIds)")
    suspend fun deleteSyncedNotIn(serverIds: List<Int>)

    @Transaction
    suspend fun upsertByRemoteId(entity: UnitEntity): Int {
        val remoteId = requireNotNull(entity.remoteId) {
            "upsertByRemoteId requires a non-null remoteId"
        }
        val existing = findByRemoteId(remoteId) ?: findByName(entity.name.lowercase())
        return if (existing != null) {
            update(entity.copy(localId = existing.localId))
            existing.localId
        } else {
            insert(entity).toInt()
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingDelete(tombstone: UnitPendingDeleteEntity)

    @Query("SELECT * FROM unit_pending_delete")
    suspend fun getPendingDeletes(): List<UnitPendingDeleteEntity>

    @Query("SELECT remoteId FROM unit_pending_delete")
    suspend fun getPendingDeleteRemoteIds(): List<Int>

    @Query("DELETE FROM unit_pending_delete WHERE remoteId = :remoteId")
    suspend fun deletePendingDelete(remoteId: Int)

    @Query("DELETE FROM unit")
    suspend fun deleteAll()

    @Query("DELETE FROM unit_pending_delete")
    suspend fun deleteAllPendingDeletes()
}
