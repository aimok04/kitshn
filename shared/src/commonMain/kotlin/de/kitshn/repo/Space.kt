package de.kitshn.repo

import co.touchlab.kermit.Logger
import de.kitshn.api.tandoor.model.TandoorSpace
import de.kitshn.session.TandoorSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration

private const val TAG = "SpaceRepo"

class SpaceRepo(
    private val session: TandoorSession,
    periodicInterval: Duration? = null,
) : SyncableRepo(periodicInterval) {
    private val _current = MutableStateFlow<TandoorSpace?>(null)
    val current: Flow<TandoorSpace?> = _current.asStateFlow()

    private var spacesCache: List<TandoorSpace>? = null

    override suspend fun performSync() {
        Logger.d(tag = TAG) { "Performing sync" }
        val client = session.client ?: return
        try {
            _current.value = client.space.current()
        } catch (e: Exception) {
            Logger.e(throwable = e, tag = TAG) { "Failed to sync spaces" }
        }
    }

    suspend fun spaces(forceRefresh: Boolean = false): List<TandoorSpace> {
        if (!forceRefresh) spacesCache?.let { return it }
        val client = session.client ?: return emptyList()
        return try {
            client.space.listAll().also { spacesCache = it }
        } catch (e: Exception) {
            Logger.e(throwable = e, tag = TAG) { "Failed to fetch spaces" }
            spacesCache ?: emptyList()
        }
    }

    suspend fun switch(spaceId: Int): Boolean {
        val client = session.client ?: return false
        return try {
            client.space.switch(spaceId)
            _current.value = client.space.current()
            true
        } catch (e: Exception) {
            Logger.e(throwable = e, tag = TAG) { "Failed to switch space" }
            false
        }
    }
}
