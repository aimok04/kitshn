package de.kitshn.repo

import co.touchlab.kermit.Logger
import de.kitshn.AppDatabase
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
    db: AppDatabase,
    private val session: TandoorSession,
    periodicInterval: Duration? = null,
) : SyncableRepo(db.repoMetaDao(), periodicInterval) {
    override val repoMetaName: String = "space"
    override val reconcileCapabilities = emptySet<ReconcileCapability>()

    val client: TandoorClient? get() = session.client

    private val _current = MutableStateFlow<TandoorSpace?>(null)
    val current: StateFlow<TandoorSpace?> = _current.asStateFlow()

    private val _spaces = MutableStateFlow<List<TandoorSpace>>(emptyList())
    val spaces: StateFlow<List<TandoorSpace>> = _spaces.asStateFlow()

    private val _members = MutableStateFlow<Map<Int, List<TandoorUser>>>(emptyMap())
    val members: StateFlow<Map<Int, List<TandoorUser>>> = _members.asStateFlow()

    override suspend fun performFullReconcile(): ReconcileResult? {
        val client = session.client ?: return null
        coroutineScope {
            launch { fetchCurrent(client) }
            launch { fetchSpaces(client) }
        }
        return ReconcileResult()
    }

    private suspend fun fetchCurrent(client: TandoorClient) {
        runCatching { client.space.current() }
            .onSuccess { _current.value = it }
            .onFailure { Logger.e(TAG, it) { "Current space sync failed" } }
    }

    private suspend fun fetchSpaces(client: TandoorClient) {
        runCatching { client.space.retrieve().results }
            .onSuccess { _spaces.value = it }
            .onFailure { Logger.e(TAG, it) { "Spaces sync failed" } }
    }

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
