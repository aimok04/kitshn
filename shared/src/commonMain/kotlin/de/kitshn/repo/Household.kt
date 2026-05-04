package de.kitshn.repo

import co.touchlab.kermit.Logger
import de.kitshn.api.tandoor.model.TandoorHousehold
import de.kitshn.api.tandoor.model.TandoorUserSpace
import de.kitshn.session.TandoorSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration

private const val TAG = "HouseholdRepo"

class HouseholdRepo(
    private val session: TandoorSession,
    periodicInterval: Duration? = null,
) : SyncableRepo(periodicInterval) {
    // all households are rarely viewed, so we do not need to sync them
    private var householdsCache: List<TandoorHousehold>? = null

    private val _userSpace = MutableStateFlow<TandoorUserSpace?>(null)
    val userSpace: Flow<TandoorUserSpace?> = _userSpace.asStateFlow()

    val current: Flow<TandoorHousehold?> = _userSpace.map { it?.household }

    override suspend fun performSync() {
        Logger.d(tag = TAG) { "Performing sync" }
        val client = session.client ?: return
        try {
            _userSpace.value = client.userSpace.allPersonal().firstOrNull { it.active }
        } catch (e: Exception) {
            Logger.e(throwable = e, tag = TAG) { "Failed to sync current household" }
        }
    }

    suspend fun households(forceRefresh: Boolean = false): List<TandoorHousehold> {
        if (!forceRefresh) householdsCache?.let { return it }
        val client = session.client ?: return emptyList()
        return try {
            client.household.retrieve().results.also { householdsCache = it }
        } catch (e: Exception) {
            Logger.e(throwable = e, tag = TAG) { "Failed to fetch households" }
            householdsCache ?: emptyList()
        }
    }

    suspend fun switch(householdId: Int): Boolean {
        val client = session.client ?: return false
        val current = _userSpace.value ?: return false
        return try {
            _userSpace.value = client.userSpace.setHousehold(current.id, householdId)
            true
        } catch (e: Exception) {
            Logger.e(throwable = e, tag = TAG) { "Failed to switch household" }
            false
        }
    }

    suspend fun create(name: String): TandoorHousehold? {
        val client = session.client ?: return null
        return try {
            val created = client.household.create(name)
            householdsCache = householdsCache?.plus(created)
            switch(created.id)
            created
        } catch (e: Exception) {
            Logger.e(throwable = e, tag = TAG) { "Failed to create household" }
            null
        }
    }

    suspend fun rename(id: Int, name: String): TandoorHousehold? {
        val client = session.client ?: return null
        return try {
            val updated = client.household.update(id, name)
            householdsCache = householdsCache?.map { if (it.id == id) updated else it }
            if (_userSpace.value?.household?.id == id) {
                _userSpace.value = _userSpace.value?.copy(household = updated)
            }
            updated
        } catch (e: Exception) {
            Logger.e(throwable = e, tag = TAG) { "Failed to rename household" }
            null
        }
    }

    suspend fun delete(id: Int): Boolean {
        val client = session.client ?: return false
        return try {
            client.household.delete(id)
            householdsCache = householdsCache?.filterNot { it.id == id }
            true
        } catch (e: Exception) {
            Logger.e(throwable = e, tag = TAG) { "Failed to delete household" }
            false
        }
    }
}
