package de.kitshn.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import de.kitshn.SettingsViewModel
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorCredentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Owns the currently-active Tandoor session: the [TandoorClient] and its
 * persisted credentials.
 *
 * Exposed as a Koin singleton so repositories and view models share one source
 * of truth instead of threading the client through every call. [client] is
 * backed by Compose state, so composables that read it recompose on change.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class TandoorSession(
    private val settings: SettingsViewModel,
    networkObserver: NetworkObserver,
    scope: CoroutineScope,
) {
    var client: TandoorClient? by mutableStateOf(null)

    val isSignedIn: Boolean get() = client != null

    /**
     * `true` iff the device has connectivity AND the last Tandoor call wasn't a
     * transport failure. HTTP 4xx/5xx don't flip this — the server is reachable,
     * it just refused. Used to gate destructive operations like delete().
     */
    val isOnline: StateFlow<Boolean> =
        combine(
            networkObserver.isConnected,
            snapshotFlow { client }.flatMapLatest { it?.lastCallSucceeded ?: flowOf(true) },
        ) { connected, callOk -> connected && callOk }
            .stateIn(scope, SharingStarted.Eagerly, true)

    suspend fun loadPersistedCredentials(): TandoorCredentials? =
        settings.getTandoorCredentials.first()

    /** Attach a client built from previously-persisted credentials (app startup). */
    fun hydrate(credentials: TandoorCredentials) {
        if (client == null) client = TandoorClient(credentials)
    }

    /** Completed sign-in: new client + persist credentials. */
    fun signIn(client: TandoorClient, credentials: TandoorCredentials) {
        this.client = client
        settings.saveTandoorCredentials(credentials)
    }

    /** Drop client + wipe persisted credentials. */
    fun signOut() {
        client = null
        settings.saveTandoorCredentials(null)
    }
}
