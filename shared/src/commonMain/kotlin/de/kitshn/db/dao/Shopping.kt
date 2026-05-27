package de.kitshn.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import de.kitshn.db.entity.ShoppingItemEntity
import de.kitshn.db.entity.ShoppingItemWithRelations
import de.kitshn.db.entity.ShoppingTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {
    @Transaction
    @Query("""
        SELECT * FROM ShoppingItemEntity
        WHERE id NOT IN (SELECT entryId FROM ShoppingTransactionEntity WHERE action = 'DELETE')
        ORDER BY `order` ASC
    """)
    fun getAllAsFlow(): Flow<List<ShoppingItemWithRelations>>

    @Insert
    suspend fun insertReturningId(item: ShoppingItemEntity): Long

    @Update
    suspend fun update(item: ShoppingItemEntity)

    @Upsert
    suspend fun upsertAll(items: List<ShoppingItemEntity>)

    @Query("SELECT * FROM ShoppingItemEntity WHERE id = :localId LIMIT 1")
    suspend fun findByLocalId(localId: Int): ShoppingItemEntity?

    @Query("SELECT * FROM ShoppingItemEntity WHERE remoteId = :remoteId LIMIT 1")
    suspend fun findByRemoteId(remoteId: Int): ShoppingItemEntity?

    // Drops rows that were once synced (remoteId IS NOT NULL) but no longer appear in
    // the server response, while leaving offline stubs (remoteId IS NULL) alone.
    // `protectedIds` covers any rows currently mid-mutation locally (have queued txns).
    @Query("""
        DELETE FROM ShoppingItemEntity
        WHERE remoteId IS NOT NULL
          AND remoteId NOT IN (:keepRemoteIds)
          AND id NOT IN (:protectedIds)
    """)
    suspend fun deleteSyncedNotIn(
        keepRemoteIds: List<Int>,
        protectedIds: List<Int>,
    )

    @Query("UPDATE ShoppingItemEntity SET checked = :checked WHERE id = :id")
    suspend fun updateChecked(
        id: Int,
        checked: Boolean,
    )

    @Query("UPDATE ShoppingItemEntity SET amount = :amount WHERE id = :id")
    suspend fun updateAmount(
        id: Int,
        amount: Double,
    )

    @Query("UPDATE ShoppingItemEntity SET unit_id = :unitId WHERE id = :id")
    suspend fun updateUnit(
        id: Int,
        unitId: Int?,
    )

    @Query("UPDATE ShoppingItemEntity SET lastSyncError = :error WHERE id = :id")
    suspend fun updateSyncError(
        id: Int,
        error: String?,
    )

    @Query("SELECT remoteId FROM ShoppingItemEntity WHERE id = :localId LIMIT 1")
    suspend fun remoteIdByLocalId(localId: Int): Int?

    @Query("SELECT id FROM ShoppingItemEntity WHERE remoteId = :remoteId LIMIT 1")
    suspend fun localIdByRemoteId(remoteId: Int): Int?

    @Transaction
    @Query("""
        SELECT * FROM ShoppingItemEntity
        WHERE id = :id
          AND id NOT IN (SELECT entryId FROM ShoppingTransactionEntity WHERE action = 'DELETE')
    """)
    suspend fun getWithRelationsById(id: Int): ShoppingItemWithRelations?

    @Transaction
    @Query("""
        SELECT * FROM ShoppingItemEntity
        WHERE remoteId IS NULL
          AND id NOT IN (SELECT entryId FROM ShoppingTransactionEntity WHERE action = 'DELETE')
        ORDER BY id ASC
    """)
    suspend fun getPendingCreates(): List<ShoppingItemWithRelations>

    @Query("DELETE FROM ShoppingItemEntity WHERE id = :localId")
    suspend fun delete(localId: Int)

    @Transaction
    suspend fun upsertByRemoteId(entity: ShoppingItemEntity): Int {
        val remoteId = requireNotNull(entity.remoteId) {
            "upsertByRemoteId requires a non-null remoteId"
        }
        val existingByRemote = findByRemoteId(remoteId)
        if (existingByRemote != null) {
            update(entity.copy(id = existingByRemote.id))
            return existingByRemote.id
        }
        if (entity.id != 0 && findByLocalId(entity.id) != null) {
            update(entity)
            return entity.id
        }
        return insertReturningId(entity.copy(id = 0)).toInt()
    }

    @Insert
    suspend fun insertTransaction(tx: ShoppingTransactionEntity)

    @Query("SELECT * FROM ShoppingTransactionEntity ORDER BY id ASC")
    suspend fun getPendingTransactions(): List<ShoppingTransactionEntity>

    @Query("DELETE FROM ShoppingTransactionEntity WHERE id = :id")
    suspend fun deleteTransaction(id: Int)

    @Query("DELETE FROM ShoppingTransactionEntity WHERE entryId = :entryId")
    suspend fun deleteTransactionsForEntry(entryId: Int)
}
