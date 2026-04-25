package de.kitshn.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import de.kitshn.db.entity.SupermarketCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupermarketCategoryDao {
    @Upsert
    suspend fun upsertAll(categories: List<SupermarketCategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIfAbsent(categories: List<SupermarketCategoryEntity>)

    @Query("SELECT * FROM supermarket_category ORDER BY name ASC")
    fun getAll(): Flow<List<SupermarketCategoryEntity>>

    @Query("DELETE FROM supermarket_category WHERE id NOT IN (:ids)")
    suspend fun deleteAllExcept(ids: List<Int>)
}
