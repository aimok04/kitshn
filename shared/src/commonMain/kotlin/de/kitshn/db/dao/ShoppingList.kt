package de.kitshn.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import de.kitshn.db.entity.ShoppingListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Upsert
    suspend fun upsertAll(lists: List<ShoppingListEntity>)

    @Query("SELECT * FROM shopping_list ORDER BY name ASC")
    fun getAllAsFlow(): Flow<List<ShoppingListEntity>>

    @Query("DELETE FROM shopping_list WHERE id NOT IN (:serverIds)")
    suspend fun deleteSyncedNotIn(serverIds: List<Long>)

    @Query("DELETE FROM shopping_list")
    suspend fun deleteAll()
}
