package de.kitshn.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import de.kitshn.db.entity.RepoMetaEntity

@Dao
interface RepoMetaDao {
    @Upsert
    suspend fun upsert(meta: RepoMetaEntity)

    @Query("SELECT * FROM repo_meta WHERE repoName = :repoName")
    suspend fun get(repoName: String): RepoMetaEntity?

    @Query("DELETE FROM repo_meta")
    suspend fun deleteAll()
}
