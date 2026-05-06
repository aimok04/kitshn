package de.kitshn.repo

import co.touchlab.kermit.Logger
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorHousehold
import de.kitshn.api.tandoor.model.TandoorUserSpace
import de.kitshn.api.tandoor.route.TandoorUser
import de.kitshn.session.TandoorSession
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration

private const val TAG = "HouseholdRepo"

class HouseholdRepo(
    private val session: TandoorSession,
    periodicInterval: Duration? = null,
) : SyncableRepo(periodicInterval) {
    private val _households = MutableStateFlow<List<TandoorHousehold>>(emptyList())
    val households: StateFlow<List<TandoorHousehold>> = _households.asStateFlow()

    private val _userSpace = MutableStateFlow<TandoorUserSpace?>(null)
    val userSpace: Flow<TandoorUserSpace?> = _userSpace.asStateFlow()

    private val _members = MutableStateFlow<Map<Int, List<TandoorUser>>>(emptyMap())
    val members: StateFlow<Map<Int, List<TandoorUser>>> = _members.asStateFlow()

    val current: Flow<TandoorHousehold?> = _userSpace.map { it?.household }

    override suspend fun performSync() {
        val client = session.client ?: return

        coroutineScope {
            launch { fetchUserSpace(client) }
            launch { fetchHouseholds(client) }
        }
    }

    suspend fun syncMembers() {
        val client = session.client ?: return

        val accumulator = mutableMapOf<Int, MutableList<TandoorUser>>()
        runCatching {
            client.userSpace.retrieve(
                onPageReceived = { page ->
                    page.forEach { space ->
                        val householdId = space.household?.id ?: return@forEach
                        val user = space.user ?: return@forEach
                        accumulator.getOrPut(householdId) { mutableListOf() }.add(user)
                    }
                    _members.value = accumulator.mapValues { it.value.toList() }
                }
            )
        }.onFailure { Logger.e(TAG, it) { "Members sync failed" } }
    }

    private suspend fun fetchUserSpace(client: TandoorClient) {
        runCatching {
            client.userSpace.allPersonal().firstOrNull { it.active }
        }.onSuccess { _userSpace.value = it }
            .onFailure { Logger.e(TAG, it) { "UserSpace sync failed" } }
    }

    suspend fun fetchHouseholds(client: TandoorClient): List<TandoorHousehold> {
        return runCatching {
            client.household.retrieve().results
        }.onSuccess { _households.value = it }
            .onFailure { Logger.e(TAG, it) { "Households fetch failed" } }
            .getOrDefault(_households.value)
    }

    suspend fun switch(householdId: Int): Boolean {
        val client = session.client ?: return false
        val current = _userSpace.value ?: return false

        return runCatching {
            _userSpace.value = client.userSpace.setHousehold(current, householdId)
            true
        }.getOrDefault(false)
    }

    suspend fun create(name: String): TandoorHousehold? {
        val client = session.client ?: return null

        return runCatching {
            val created = client.household.create(name)
            _households.value = _households.value.plus(created)
            switch(created.id)
            created
        }.getOrNull()
    }

    suspend fun rename(id: Int, name: String): TandoorHousehold? {
        val client = session.client ?: return null

        return runCatching{
            val updated = client.household.update(id, name)
            _households.value = _households.value.map { if (it.id == id) updated else it }

            if (_userSpace.value?.household?.id == id) {
                _userSpace.value = _userSpace.value?.copy(household = updated)
            }
            updated
        }.getOrNull()
    }

    suspend fun delete(id: Int): Boolean {
        val client = session.client ?: return false

        return runCatching {
            client.household.delete(id)
            _households.value = _households.value.filterNot { it.id == id }
            _members.value = _members.value.filterKeys { it != id }
            true
        }.getOrElse { false }
    }
}
