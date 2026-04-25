package de.kitshn.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import de.kitshn.db.entity.FoodEntity
import de.kitshn.db.entity.FoodWithRelations
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Upsert
    suspend fun upsertAll(foods: List<FoodEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIfAbsent(foods: List<FoodEntity>)

    @Transaction
    @Query("SELECT * FROM food WHERE id = :id")
    suspend fun getWithRelationsById(id: Int): FoodWithRelations?

    @Transaction
    @Query("SELECT * FROM food ORDER BY name ASC")
    fun getAllWithRelations(): Flow<List<FoodWithRelations>>

    @Query("SELECT COUNT(*) FROM food")
    suspend fun count(): Int

    @Query("SELECT COALESCE(MIN(id), 0) - 1 FROM food WHERE id < 0")
    suspend fun nextLocalId(): Int

    @Query("DELETE FROM food WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM food WHERE id NOT IN (:ids)")
    suspend fun deleteAllExcept(ids: List<Int>)

    // mostly only used when we have alooot of foods
    // which we do not want to keep in mem cached
    @Transaction
    @Query("""
        SELECT * FROM food 
        WHERE name LIKE '%' || :query || '%' 
        OR plural_name LIKE '%' || :query || '%'
        ORDER BY 
            CASE 
                WHEN LOWER(name) = LOWER(:query) THEN 0
                WHEN LOWER(name) LIKE LOWER(:query) || '%' THEN 1
                ELSE 2 
            END, 
            LENGTH(name) ASC 
        LIMIT 50
    """)
    fun searchWithRelations(query: String): Flow<List<FoodWithRelations>>
}
