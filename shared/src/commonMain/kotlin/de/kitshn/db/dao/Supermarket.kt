package de.kitshn.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import de.kitshn.db.entity.SupermarketCategoryToSupermarketEntity
import de.kitshn.db.entity.SupermarketEntity
import de.kitshn.db.entity.SupermarketWithCategories
import kotlinx.coroutines.flow.Flow

@Dao
interface SupermarketDao {
    @Upsert
    suspend fun upsertSupermarkets(entities: List<SupermarketEntity>)

    @Insert()
    suspend fun insertSupermarket(entity: SupermarketEntity): Long

    @Update
    suspend fun updateSupermarket(entity: SupermarketEntity)

    @Upsert
    suspend fun upsertJoins(entities: List<SupermarketCategoryToSupermarketEntity>)

    @Query("DELETE FROM supermarket WHERE remoteId NOT IN (:remoteIds) OR remoteId IS NULL")
    suspend fun deleteSupermarketsNotIn(remoteIds: List<Int>)

    @Query("DELETE FROM supermarket_category_to_supermarket WHERE supermarketLocalId = :supermarketLocalId AND id NOT IN (:keepIds)")
    suspend fun deleteJoinsForSupermarketNotIn(supermarketLocalId: Int, keepIds: List<Int>)

    @Query("DELETE FROM supermarket_category_to_supermarket WHERE supermarketLocalId = :supermarketLocalId")
    suspend fun deleteAllJoinsForSupermarket(supermarketLocalId: Int)

    @Transaction
    @Query("SELECT * FROM supermarket ORDER BY name ASC")
    fun getAllWithCategoriesAsFlow(): Flow<List<SupermarketWithCategories>>

    @Query("SELECT id FROM supermarket WHERE remoteId = :remoteId LIMIT 1")
    suspend fun localIdByRemoteId(remoteId: Int): Int?

    @Query("DELETE FROM supermarket")
    suspend fun deleteAll()

    @Query("DELETE FROM supermarket_category_to_supermarket")
    suspend fun deleteAllJoins()

    @Transaction
    suspend fun upsertByRemoteId(entity: SupermarketEntity): Int {
        val remoteId = requireNotNull(entity.remoteId) {
            "upsertByRemoteId requires a non-null remoteId"
        }
        val existingId = localIdByRemoteId(remoteId)
        return if (existingId != null) {
            updateSupermarket(entity.copy(localId = existingId))
            existingId
        } else {
            insertSupermarket(entity).toInt()
        }
    }
}
