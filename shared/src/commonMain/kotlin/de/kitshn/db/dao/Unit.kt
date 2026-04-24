package de.kitshn.db.dao

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import de.kitshn.db.entity.UnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitDao {
    @Upsert
    suspend fun upsertAll(units: List<UnitEntity>)

    @Query("SELECT * FROM unit")
    fun getAll(): Flow<List<UnitEntity>>

    @Query("DELETE FROM unit WHERE id NOT IN (:ids)")
    suspend fun deleteAllExcept(ids: List<Int>)

    @Query("SELECT COALESCE(MIN(id), 0) - 1 FROM unit WHERE id < 0")
    suspend fun nextLocalId(): Int

    @Query("DELETE FROM unit WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("SELECT * FROM unit WHERE name LIKE '%' || :query || '%' OR plural_name LIKE '%' || :query || '%' ORDER BY name ASC LIMIT 5")
    fun search(query: String): Flow<List<UnitEntity>>
}
