@file:OptIn(ExperimentalTime::class)

package de.kitshn

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import co.touchlab.kermit.Logger
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorCredentials
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.api.tandoor.reqAny
import androidx.compose.runtime.snapshotFlow
import de.kitshn.repo.ShoppingRepo
import de.kitshn.repo.SyncableRepo
import de.kitshn.repo.UnitRepo
import de.kitshn.session.TandoorSession
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.route.main.clearRememberAlternateNavController
import de.kitshn.ui.state.clearForeverRememberMutableStateList
import de.kitshn.ui.state.clearForeverRememberNotSavable
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Runtime-only parameters for [KitshnViewModel] (callbacks supplied by the UI
 * layer). Passed via `parametersOf(...)` when resolving the VM.
 */
data class KitshnViewModelArgs(
    /** Called before potential onboarding. Aborts onboarding if `true` is returned. */
    val onBeforeCredentialsCheck: (credentials: TandoorCredentials?) -> Boolean = { false },

    /** Called after onboarding checks and completed onboarding. */
    val onLaunched: () -> Unit = { }
)

class KitshnViewModel(
    val db: AppDatabase,
    val settings: SettingsViewModel,
    private val session: TandoorSession,
    val unitRepo: UnitRepo,
    val shoppingRepo: ShoppingRepo,
    private val applicationScope: CoroutineScope,

    val onBeforeCredentialsCheck: (credentials: TandoorCredentials?) -> Boolean = { false },
    val onLaunched: () -> Unit = { }
) : ViewModel() {

    var isTest: Boolean = false

    var navHostController: NavHostController? = null
    var mainSubNavHostController: NavHostController? = null

    var tandoorClient: TandoorClient?
        get() = session.client
        set(value) { session.client = value }

    val favorites = FavoritesViewModel()

    val uiState = UiStateModel()

    lateinit var manageIosSubscriptionView: @Composable (p: RouteParameters) -> Unit

    fun searchKeyword(id: Int) {
        viewModelScope.launch {
            navigateTo("main", "home")
            uiState.searchKeyword.set(id)
        }
    }

    fun searchCreatedBy(id: Int) {
        viewModelScope.launch {
            navigateTo("main", "home")
            uiState.searchCreatedBy.set(id)
        }
    }

    fun viewRecipe(id: Int) {
        viewModelScope.launch {
            navigateTo("main", "home")
            uiState.viewRecipe.set(id)
        }
    }

    private var initTime = 0L
    private var initComplete = false

    fun init() {
        if(initComplete) return
        initComplete = true

        initTime = Clock.System.now()
            .toEpochMilliseconds()

        startPeriodicSync()

        viewModelScope.launch {
            if(settings.getFirstRunTime.first() == -1L)
                settings.setFirstRunTime()

            val credentials = session.loadPersistedCredentials()
            if(onBeforeCredentialsCheck(credentials)) return@launch

            if(credentials == null) {
                navHostController!!.navigate("onboarding") {
                    popUpTo("main") {
                        inclusive = true
                    }
                }
                return@launch
            }

            session.hydrate(credentials)
            favorites.init(session.client!!)

            connectivityCheck()

            try {
                session.client!!.serverSettings.current()
            } catch(e: TandoorRequestsError) {
                if(e.response?.status == HttpStatusCode.NotFound) {
                    navHostController?.navigate("alert/outdatedV1Instance") {
                        popUpTo("main") {
                            inclusive = true
                        }
                    }

                    return@launch
                }

                Logger.e("KitshnViewModel.kt", e)
            } catch(e: Exception) {
                Logger.e("KitshnViewModel.kt", e)
            }

            if(settings.getOnboardingCompleted.first()) {
                onLaunched()
                return@launch
            }

            navHostController?.navigate("onboarding/welcome") {
                popUpTo("main") {
                    inclusive = true
                }
            }
        }
    }

    fun navigateTo(mainRoute: String, subRoute: String? = null) {
        if(!navHostController?.currentDestination?.route.equals(mainRoute))
            navHostController?.navigate(mainRoute)

        if(subRoute == null) return

        if(!mainSubNavHostController?.currentDestination?.route.equals(subRoute))
            mainSubNavHostController?.navigate(subRoute)
    }

    // reset app and cached/saved objects — used for example for changing spaces
    fun refreshApp() {
        viewModelScope.launch {
            uiState.blockUI = true
            delay(250)

            clearForeverRememberNotSavable()
            clearForeverRememberMutableStateList()
            clearRememberAlternateNavController()

            delay(250)
            uiState.blockUI = false
        }
    }

    // enable offline state when having connectivity issues
    fun connectivityCheck() {
        val client = session.client ?: return
        if(!uiState.isInForeground) return

        viewModelScope.launch {
            var isOffline = true

            try {
                val response = client.reqAny(
                    endpoint = "/",
                    _method = HttpMethod.Get,
                    customHttpClient = HttpClient {
                        install(HttpTimeout) {
                            requestTimeoutMillis = 2000
                        }
                    }
                )

                if(response.status == HttpStatusCode.OK)
                    isOffline = false
            } catch(_: TandoorRequestsError) {
            } catch(_: SerializationException) {
            }

            if(isOffline) {
                isOffline = true

                try {
                    val response = client.reqAny(
                        endpoint = "/",
                        _method = HttpMethod.Get,
                        customHttpClient = HttpClient {
                            install(HttpTimeout) {
                                requestTimeoutMillis = 5000
                            }
                        }
                    )

                    if(response.status == HttpStatusCode.OK)
                        isOffline = false
                } catch(_: TandoorRequestsError) {
                } catch(_: SerializationException) {
                }

                if(isOffline) {
                    uiState.offlineState.isOffline = true

                    // automatically switch to shopping page if offline
                    if((Clock.System.now().toEpochMilliseconds() - initTime) < 8000) {
                        if(navHostController?.currentDestination?.route != "main") return@launch
                        if(mainSubNavHostController?.currentDestination?.route != "home") return@launch
                        mainSubNavHostController?.navigate("shopping")
                    }
                } else {
                    uiState.offlineState.isOffline = false
                }
            } else {
                uiState.offlineState.isOffline = false
            }
        }
    }

    fun sync() {
        if (tandoorClient == null) return
        viewModelScope.launch(Dispatchers.IO) {
            coroutineScope {
                launch { shoppingRepo.sync() }
                launch { unitRepo.sync() }
            }
        }
    }

    private val syncableRepos: List<SyncableRepo>
        get() = listOf(shoppingRepo)

    private var periodicSyncStarted = false

    // TODO: this should be refactored to somewhere else
    fun startPeriodicSync() {
        if (periodicSyncStarted) return
        periodicSyncStarted = true

        syncableRepos.forEach { repo ->
            val interval = repo.periodicInterval ?: return@forEach
            viewModelScope.launch(Dispatchers.IO) {
                while (true) {
                    delay(interval)
                    if (uiState.isInForeground && tandoorClient != null) repo.sync()
                }
            }
        }

        viewModelScope.launch {
            snapshotFlow { uiState.isInForeground }
                .drop(1)
                .filter { it }
                .collect { sync() }
        }
    }

    // Cleanup stale local data infrequently
    fun reconcile() {
        if (tandoorClient == null) return
        viewModelScope.launch(Dispatchers.IO) {
            coroutineScope {
                launch { unitRepo.sync() }
            }
        }
    }

    fun signIn(client: TandoorClient, credentials: TandoorCredentials) {
        session.signIn(client, credentials)

        navHostController?.navigate("onboarding/welcome")

        favorites.init(client)
        connectivityCheck()
    }

    fun signOut() {
        settings.setOnboardingCompleted(false)
        session.signOut()
        // Runs on the application scope so the teardown completes even if the
        // VM's own scope is cancelled by navigation away from the host screen.
        applicationScope.launch {
            db.closeAndDelete()
        }
    }

}
