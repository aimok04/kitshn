package de.kitshn.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Per-repo sync bookkeeping.
//
// Delta sync (DeltaSyncableRepo): updatedAtWatermark + lastSyncedAt are the cursor and
// last-run time. recentDeltaItemCount stores the size of the most recent delta — used to
// adaptively stretch the bucket-sweep cadence when data is stable (count == 0).
//
// Bucket reconcile (SyncableRepo): cursorPage is the next page to fetch under the
// Bucketed strategy (1-indexed, wraps after the last page). cursorPageSize records the
// pageSize the cursor was advanced against — if the strategy is later reconfigured with
// a different pageSize, the cursor is meaningless and gets reset. lastReconciledAt gates
// the reconcile min-interval.
@Entity(tableName = "repo_meta")
data class RepoMetaEntity(
    @PrimaryKey val repoName: String,
    val updatedAtWatermark: String? = null,
    val lastSyncedAt: Long = 0L,
    val recentDeltaItemCount: Int = -1,
    val cursorPage: Int = 1,
    val cursorPageSize: Int? = null,
    val lastReconciledAt: Long = 0L,
)
