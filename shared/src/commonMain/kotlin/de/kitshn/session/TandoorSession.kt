package de.kitshn.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.kitshn.SettingsViewModel
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorCredentials
import kotlinx.coroutines.flow.first

/**
 * Owns the currently-active Tandoor session: the [TandoorClient] and its
 * persisted credentials.
 *
 * Exposed as a Koin singleton so repositories and view models share one source
 * of truth instead of threading the client through every call. [client] is
 * backed by Compose state, so composables that read it recompose on change.
 */
class TandoorSession(
    private val settings: SettingsViewModel,
) {
    var client: TandoorClient? by mutableStateOf(null)

    val isSignedIn: Boolean get() = client != null

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
