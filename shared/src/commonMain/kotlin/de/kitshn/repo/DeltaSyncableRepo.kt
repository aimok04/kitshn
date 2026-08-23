@file:OptIn(ExperimentalTime::class)

package de.kitshn.repo

import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.db.dao.RepoMetaDao
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime


/**
 * Output of [DeltaSyncableRepo.performDeltaSync].
 *
 * @property nextWatermark Next Watermark. Null leaves the last watermark
 * @property itemCount how many items the delta returned.
 */
data class DeltaResult(val nextWatermark: String?, val itemCount: Int)

abstract class DeltaSyncableRepo(
    repoMetaDao: RepoMetaDao,
    periodicInterval: Duration? = null,
    minInterval: Duration = 30.seconds,
    reconcileInterval: Duration = 7.days,
) : SyncableRepo(repoMetaDao, periodicInterval, minInterval, reconcileInterval) {

    open val deltaPageSize: Int = 200
    open val deltaMaxPages: Int = 5

    override suspend fun performSyncAction() {
        if (withinDeltaInterval()) return
        try {
            executeDeltaSync(deltaMaxPages)
        } catch (e: TandoorRequestsError) {
            logSyncFailure(e, "deltaSync")
        }
    }

    suspend fun deltaSync(maxPages: Int = deltaMaxPages) {
        if (withinDeltaInterval()) return
        if (!mutex.tryLock()) return
        try {
            if (withinDeltaInterval()) return
            executeDeltaSync(maxPages)
        } catch (e: TandoorRequestsError) {
            logSyncFailure(e, "deltaSync")
        } finally {
            mutex.unlock()
        }
    }

    protected abstract suspend fun performDeltaSync(
        watermark: String?,
        maxPages: Int,
        pageSize: Int,
    ): DeltaResult?

    suspend fun resetDelta() {
        updateMeta { it.copy(updatedAtWatermark = null) }
    }

    private suspend fun executeDeltaSync(maxPages: Int) {
        val meta = getMeta()
        val result = performDeltaSync(meta?.updatedAtWatermark, maxPages, deltaPageSize) ?: return
        updateMeta {
            it.copy(
                updatedAtWatermark = result.nextWatermark ?: it.updatedAtWatermark,
                lastSyncedAt = Clock.System.now().toEpochMilliseconds(),
                recentDeltaItemCount = result.itemCount,
            )
        }
    }

    private suspend fun withinDeltaInterval(): Boolean {
        val meta = getMeta() ?: return false
        val now = Clock.System.now().toEpochMilliseconds()
        return now - meta.lastSyncedAt < minInterval.inWholeMilliseconds
    }
}
