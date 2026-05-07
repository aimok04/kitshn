package de.kitshn.repo

import co.touchlab.kermit.Logger
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.PartialTandoorSpace
import de.kitshn.api.tandoor.model.TandoorSpace
import de.kitshn.api.tandoor.route.TandoorUser
import de.kitshn.session.TandoorSession
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration

private const val TAG = "SpaceRepo"

class SpaceRepo(
    private val session: TandoorSession,
    periodicInterval: Duration? = null,
) : SyncableRepo(periodicInterval) {
    private val _current = MutableStateFlow<TandoorSpace?>(null)
    val current: StateFlow<TandoorSpace?> = _current.asStateFlow()

    private val _spaces = MutableStateFlow<List<TandoorSpace>>(emptyList())
    val spaces: StateFlow<List<TandoorSpace>> = _spaces.asStateFlow()

    /** Members of each known space, keyed by space id, populated by [syncMembers]. */
    private val _members = MutableStateFlow<Map<Int, List<TandoorUser>>>(emptyMap())
    val members: StateFlow<Map<Int, List<TandoorUser>>> = _members.asStateFlow()

    override suspend fun performSync() {
        val client = session.client ?: return

        coroutineScope {
            launch { fetchCurrent(client) }
        }
    }

    private suspend fun fetchCurrent(client: TandoorClient) {
        runCatching { client.space.current() }
            .onSuccess { _current.value = it }
            .onFailure { Logger.e(TAG, it) { "Current space sync failed" } }
    }

    /**
     * On-demand fetch of the full space list.
     */
    suspend fun syncSpaces() {
        val client = session.client ?: return
        runCatching { client.space.retrieve().results }
            .onSuccess { _spaces.value = it }
            .onFailure { Logger.e(TAG, it) { "Spaces sync failed" } }
    }

    /**
     * On-demand fetch of space members via the user-space endpoint.
     */
    suspend fun syncMembers() {
        val client = session.client ?: return

        val accumulator = mutableMapOf<Int, MutableList<TandoorUser>>()
        runCatching {
            client.userSpace.retrieve(
                onPageReceived = { page ->
                    page.forEach { userSpace ->
                        val spaceId = userSpace.space ?: return@forEach
                        val user = userSpace.user ?: return@forEach
                        accumulator.getOrPut(spaceId) { mutableListOf() }.add(user)
                    }
                    _members.value = accumulator.mapValues { it.value.toList() }
                }
            )
        }.onFailure { Logger.e(TAG, it) { "Members sync failed" } }
    }

    /**
     * Switch the active space of a user. App should be reloaded afterward
     */
    suspend fun switch(spaceId: Int): Boolean {
        val client = session.client ?: return false

        return runCatching {
            client.space.switch(spaceId)
            _current.value = client.space.current()
            true
        }.getOrElse {
            Logger.e(TAG, it) { "Failed to switch space" }
            false
        }
    }

    /**
     * Create a new space, this requires some permissions
     */
    suspend fun create(partial: PartialTandoorSpace): TandoorSpace? {
        val client = session.client ?: return null

        return runCatching {
            val created = client.space.create(partial)
            _spaces.value = _spaces.value.plus(created)
            created
        }.getOrElse {
            Logger.e(TAG, it) { "Failed to create space" }
            null
        }
    }

    /**
     * Update an existing space, this requires some permissions
     */
    suspend fun update(id: Int, partial: PartialTandoorSpace): TandoorSpace? {
        val client = session.client ?: return null

        return runCatching {
            val updated = client.space.update(id, partial)
            _spaces.value = _spaces.value.map { if (it.id == id) updated else it }
            if (_current.value?.id == id) _current.value = updated
            updated
        }.getOrElse {
            Logger.e(TAG, it) { "Failed to update space" }
            null
        }
    }

    suspend fun rename(id: Int, name: String): TandoorSpace? =
        update(id, PartialTandoorSpace(name = name))

    /**
     * Delete a space, this requires some permissions
     */
    suspend fun delete(id: Int): Boolean {
        val client = session.client ?: return false

        return runCatching {
            client.space.delete(id)
            _spaces.value = _spaces.value.filterNot { it.id == id }
            _members.value = _members.value.filterKeys { it != id }
            true
        }.getOrElse {
            Logger.e(TAG, it) { "Failed to delete space" }
            false
        }
    }
}
