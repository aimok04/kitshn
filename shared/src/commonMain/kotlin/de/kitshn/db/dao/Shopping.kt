package de.kitshn.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import de.kitshn.api.tandoor.model.TandoorUnit
import de.kitshn.db.entity.ShoppingItemEntity
import de.kitshn.db.entity.ShoppingItemWithRelations
import de.kitshn.db.entity.ShoppingTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {
    @Transaction
    @Query("SELECT * FROM ShoppingItemEntity ORDER BY `order` ASC")
    fun getAllAsFlow(): Flow<List<ShoppingItemWithRelations>>

    @Upsert
    suspend fun upsertAll(items: List<ShoppingItemEntity>)

    @Query("DELETE FROM ShoppingItemEntity")
    suspend fun deleteAll()

    @Query("DELETE FROM ShoppingItemEntity WHERE id NOT IN (:ids) AND id NOT IN (:excludeIds)")
    suspend fun deleteAllExcept(
        ids: List<Int>,
        excludeIds: List<Int>,
    )

    // Replace local state with remote. Uses upsert to keep observers clean of
    // wipes. Allows to protect certain ids e.g. checked locally created items
    @Transaction
    suspend fun syncRemoteItems(
        items: List<ShoppingItemEntity>,
        protectedIds: List<Int> = listOf(),
    ) {
        if (items.isEmpty() && protectedIds.isEmpty()) {
            deleteAll()
            return
        }
        upsertAll(items)
        deleteAllExcept(items.map { it.id }, protectedIds)
    }

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

    @Transaction
    @Query("SELECT * FROM ShoppingItemEntity WHERE id = :id")
    suspend fun getWithRelationsById(id: Int): ShoppingItemWithRelations?

    @Query("SELECT COALESCE(MIN(id), 0) - 1 FROM ShoppingItemEntity WHERE id < 0")
    suspend fun nextLocalId(): Int

    @Query("DELETE FROM ShoppingItemEntity WHERE id = :id")
    suspend fun delete(id: Int)

    @Insert
    suspend fun insertTransaction(tx: ShoppingTransactionEntity)

    @Query("SELECT * FROM ShoppingTransactionEntity ORDER BY id ASC")
    suspend fun getPendingTransactions(): List<ShoppingTransactionEntity>

    @Query("DELETE FROM ShoppingTransactionEntity WHERE id = :id")
    suspend fun deleteTransaction(id: Int)
}
