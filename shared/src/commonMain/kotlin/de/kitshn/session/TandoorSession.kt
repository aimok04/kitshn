package de.kitshn.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.annotation.ExperimentalCoilApi
import coil3.network.ktor3.KtorNetworkFetcherFactory
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
 * [client] is a state to provide recomposition on change
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

    /** Get client from prior saved credentials */
    fun hydrate(credentials: TandoorCredentials) {
        if (client == null) {
            val newClient = TandoorClient(credentials)
            client = newClient
            updateCoilImageLoader(newClient)
        }
    }

    /** Completed sign-in new client and persist credentials */
    fun signIn(client: TandoorClient, credentials: TandoorCredentials) {
        this.client = client
        settings.saveTandoorCredentials(credentials)
        updateCoilImageLoader(client)
    }

    fun updateCredentials(credentials: TandoorCredentials) {
        val newClient = TandoorClient(credentials)
        client = newClient
        settings.saveTandoorCredentials(credentials)
        updateCoilImageLoader(newClient)
    }

    /** Drop client and wipe persisted credentials */
    @OptIn(DelicateCoilApi::class)
    fun signOut() {
        client = null
        settings.saveTandoorCredentials(null)
        // setSafe can only be used once and we *need* to clear / replace it here
        SingletonImageLoader.setUnsafe { context -> ImageLoader(context) }
    }

    @OptIn(DelicateCoilApi::class, ExperimentalCoilApi::class)
    private fun updateCoilImageLoader(client: TandoorClient) {
        val httpClient = client.httpClient
        // setSafe can only be used once, but we can update mtls later on. Disk Cache should persist
        SingletonImageLoader.setUnsafe { context ->
            ImageLoader.Builder(context)
                .components { add(KtorNetworkFetcherFactory(httpClient = httpClient)) }
                .build()
        }
    }
}
