@file:OptIn(ExperimentalTime::class)

package de.kitshn.repo

import co.touchlab.kermit.Logger
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.db.dao.RepoMetaDao
import de.kitshn.db.entity.RepoMetaEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

enum class DatasetSize { FEW, MANY, UNSPECIFIED }

/**
 * How a [SyncableRepo] reconciles on a given call.
 *
 * - [Full] pulls everything. Suitable for small repos.
 * - [Bucketed] fetches one subset per reconcile call and advances a persisted cursor,
 *   wrapping at the end.
 *
 */
sealed interface ReconcileStrategy {
    data object Full : ReconcileStrategy
    data class Bucketed(val pageSize: Int = 200) : ReconcileStrategy
}

enum class ReconcileCapability { Bucketed, Interactive }

/**
 * Output of the reconcile operations.
 *
 * @property nextPage cursor for the next reconcile call.
 * For [ReconcileStrategy.Full] this is always 1.
 * This result cursor is **only** stable if we keep the pageSize the same and
 * nothing was removed or added before this afterward.
 */
data class ReconcileResult(val nextPage: Int = 1)

abstract class SyncableRepo(
    protected val repoMetaDao: RepoMetaDao,
    val periodicInterval: Duration? = null,
    val minInterval: Duration = 30.seconds,
    val reconcileInterval: Duration = 48.hours,
) {

    protected abstract val repoMetaName: String
    protected val repoTag: String get() = "${repoMetaName}Repo"

    /** Which reconciliation strategy the repo implements. Used for strategy selection. */
    protected abstract val reconcileCapabilities: Set<ReconcileCapability>

    /** Page size handed to [performBucketReconcile] when Bucketed is selected. */
    protected open val bucketedPageSize: Int = 200

    open var datasetSize: DatasetSize = DatasetSize.UNSPECIFIED

    protected val mutex = Mutex()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private var cachedMeta: RepoMetaEntity? = null

    protected suspend fun getMeta(): RepoMetaEntity? {
        return cachedMeta ?: repoMetaDao.get(repoMetaName)?.also { cachedMeta = it }
    }

    // TODO: think about this. Is each time individual item sync fine... probably yes
    // Individual item rate-limit.
    open val itemMinInterval: Duration = 30.seconds
    private val itemSyncTimestamps = mutableMapOf<Any, Long>()

    /** Item-level TTL gate — not persisted, resets on process restart. */
    fun withinItemSyncInterval(id: Any): Boolean {
        val last = itemSyncTimestamps[id] ?: return false
        return Clock.System.now().toEpochMilliseconds() - last < itemMinInterval.inWholeMilliseconds
    }

    fun markItemSynced(id: Any) {
        itemSyncTimestamps[id] = Clock.System.now().toEpochMilliseconds()
    }

    internal fun resetLocalState() {
        cachedMeta = null
        itemSyncTimestamps.clear()
    }

    /**
     * Refresh function for background callers. By default, uses reconcile method.
     * If item support delta-sync [DeltaSyncableRepo] overrides this.
     *
     * For user-initiated refreshes (pull-to-refresh, manual reload), use [reconcileInteractive].
     */
    open suspend fun sync() {
        if (!mutex.tryLock()) return
        _isSyncing.value = true
        try {
            do {
                syncPendingBefore()
                performSyncAction()
                syncPendingAfter()
            } while (shouldRunAgain())
        } finally {
            _isSyncing.value = false
            mutex.unlock()
        }
    }

    /**
     * Hook for subclasses with mutation-driven sync: if `true`, [sync] runs another pass
     * before releasing the mutex. Lets a repo flush mutations that arrived while a launch
     * was holding the lock (and thus competing launches dropped via `tryLock`).
     */
    protected open fun shouldRunAgain(): Boolean = false

    protected open suspend fun performSyncAction() {
        if (withinReconcileInterval()) return
        try {
            executeReconcile(selectStrategy())
        } catch (e: TandoorRequestsError) {
            logSyncFailure(e, "reconcile")
        }
    }

    protected fun logSyncFailure(e: TandoorRequestsError, action: String) {
        if (e.isNetworkFailure) {
            Logger.w(tag = repoTag) { "$repoMetaName $action skipped: offline" }
        } else {
            Logger.e(e, tag = repoTag) { "$repoMetaName failed to $action" }
        }
    }

    open suspend fun syncPendingBefore() {}

    open suspend fun syncPendingAfter() {}

    /**
     * Item-level refresh. Used by [use] or manual calls.
     */
    protected open suspend fun syncItem(localId: Int) {}

    /**
     * Indicates that an item is being used or viewed and triggers an opportunistic
     * background refresh.
     */
    open fun use(localId: Int, scope: kotlinx.coroutines.CoroutineScope) {
        if (withinItemSyncInterval(localId)) return
        scope.launch {
            if (!mutex.tryLock()) return@launch
            try {
                if (withinItemSyncInterval(localId)) return@launch
                syncItem(localId)
                markItemSynced(localId)
            } finally {
                mutex.unlock()
            }
        }
    }

    /**
     * Reconcile is a more aggressive sync. It will try to fully synchronize with the server
     * even if it supports DeltaSync and remove stale entries. It is gated by [reconcileInterval].
     *
     * There are multiple strategies that will be chosen by [selectStrategy] depending on
     * item count and update frequency.
     *
     * For user-initiated fetches please use [reconcileInteractive]
     */
    suspend fun reconcile() {
        if (withinReconcileInterval()) return
        if (!mutex.tryLock()) return
        try {
            if (withinReconcileInterval()) return
            executeReconcile(selectStrategy())
        } catch (e: TandoorRequestsError) {
            logSyncFailure(e, "reconcile")
        } finally {
            mutex.unlock()
        }
    }

    protected open fun selectStrategy(): ReconcileStrategy {
        val hasBucketed = ReconcileCapability.Bucketed in reconcileCapabilities
        return when {
            hasBucketed && (datasetSize == DatasetSize.MANY) ->
                ReconcileStrategy.Bucketed(bucketedPageSize)

            else -> ReconcileStrategy.Full
        }
    }

    /**
     * User-initiated reconcile. Bypasses [reconcileInterval]
     * and dispatches via [performInteractiveReconcile]
     */
    suspend fun reconcileInteractive() {
        mutex.withLock {
            _isSyncing.value = true
            try {
                val result = performInteractiveReconcile() ?: return
                updateMeta {
                    it.copy(
                        cursorPage = result.nextPage,
                        lastReconciledAt = Clock.System.now().toEpochMilliseconds(),
                    )
                }
            } catch (e: TandoorRequestsError) {
                logSyncFailure(e, "reconcileInteractive")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    private suspend fun executeReconcile(strategy: ReconcileStrategy) {
        val meta = getMeta()
        // Reset the cursor if the configured pageSize no longer matches the cursors
        val cursor = when {
            strategy is ReconcileStrategy.Bucketed &&
                    meta?.cursorPageSize != strategy.pageSize -> 1

            else -> meta?.cursorPage ?: 1
        }
        val result = when (strategy) {
            ReconcileStrategy.Full -> performFullReconcile()
            is ReconcileStrategy.Bucketed -> performBucketReconcile(strategy, cursor)
        } ?: return
        updateMeta {
            it.copy(
                cursorPage = result.nextPage,
                cursorPageSize = (strategy as? ReconcileStrategy.Bucketed)?.pageSize
                    ?: it.cursorPageSize,
                lastReconciledAt = Clock.System.now().toEpochMilliseconds(),
            )
        }
    }

    protected abstract suspend fun performFullReconcile(): ReconcileResult?

    protected open suspend fun performBucketReconcile(
        strategy: ReconcileStrategy.Bucketed,
        cursorPage: Int,
    ): ReconcileResult? =
        error("$repoMetaName: ReconcileCapability.Bucketed declared but performBucketReconcile() not overridden")

    protected open suspend fun performInteractiveReconcile(): ReconcileResult? =
        performFullReconcile()

    private suspend fun withinReconcileInterval(): Boolean {
        val meta = getMeta() ?: return false
        val now = Clock.System.now().toEpochMilliseconds()
        return now - meta.lastReconciledAt < effectiveReconcileInterval(meta).inWholeMilliseconds
    }

    // Adaptive reconcile interval based on change count
    private fun effectiveReconcileInterval(meta: RepoMetaEntity): Duration =
        if (meta.recentDeltaItemCount == 0) reconcileInterval * 2 else reconcileInterval

    protected suspend fun updateMeta(block: (RepoMetaEntity) -> RepoMetaEntity) {
        val current = getMeta() ?: RepoMetaEntity(repoMetaName)
        val updated = block(current)
        repoMetaDao.upsert(updated)
        cachedMeta = updated
    }
}
