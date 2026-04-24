@file:OptIn(ExperimentalTime::class)

package de.kitshn.repo

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

/**
 * Base contract shared by every repository that syncs against the remote.
 *
 * - [sync] is called on synchronization events. This could be app start, periodic or
 *  manual refresh, or any other trigger. `force` bypasses the [minInterval] check.
 * - [reconcile] is called less frequently to clean up local data that no longer exists remote.
 * - [periodicInterval] When non-null, the host VM fires
 *   [sync] on this period while in foreground
 * - [minInterval] is a floor of successful syncs.
 *    Mainly due to frequent view flipping reducing API calls
 * - [reconcileInterval] is a floor of successful reconciliations.
 */
abstract class SyncableRepo(
    val periodicInterval: Duration? = null,
    val minInterval: Duration = 30.seconds,
    val reconcileInterval: Duration = 24.hours,
) {
    private val mutex = Mutex()
    private var lastSyncedAt: Long = 0L
    private var lastReconciledAt: Long = 0L

    suspend fun sync(force: Boolean = false) {
        if (!force && withinMinInterval()) return
        if (mutex.isLocked) return

        mutex.withLock {
            if (!force && withinMinInterval()) return
            performSync()
            lastSyncedAt = Clock.System.now().toEpochMilliseconds()
        }
    }

    suspend fun reconcile(force: Boolean = false) {
        if (!force && withinReconcileInterval()) return
        if (mutex.isLocked) return

        mutex.withLock {
            if (!force && withinReconcileInterval()) return
            performReconcile()
            lastReconciledAt = Clock.System.now().toEpochMilliseconds()
        }
    }

    private fun withinMinInterval(): Boolean {
        val now = Clock.System.now().toEpochMilliseconds()
        return now - lastSyncedAt < minInterval.inWholeMilliseconds
    }

    private fun withinReconcileInterval(): Boolean {
        val now = Clock.System.now().toEpochMilliseconds()
        return now - lastReconciledAt < reconcileInterval.inWholeMilliseconds
    }

    protected abstract suspend fun performSync()

    protected open suspend fun performReconcile() {
        // Default implementation does nothing
    }
}
