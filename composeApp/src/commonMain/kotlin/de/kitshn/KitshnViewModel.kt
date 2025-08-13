package de.kitshn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import co.touchlab.kermit.Logger
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorCredentials
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.api.tandoor.reqAny
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.route.main.clearRememberAlternateNavController
import de.kitshn.ui.state.clearForeverRememberMutableStateList
import de.kitshn.ui.state.clearForeverRememberNotSavable
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.SerializationException

class KitshnViewModel(
    defaultTandoorClient: TandoorClient? = null,

    /**
     * calls before potential onboarding. Aborts onboarding if true is returned
     */
    val onBeforeCredentialsCheck: (credentials: TandoorCredentials?) -> Boolean = { false },

    /**
     * after onboarding checks and completed onboarding
     */
    val onLaunched: () -> Unit = { }
) : ViewModel() {

    var navHostController: NavHostController? = null
    var mainSubNavHostController: NavHostController? = null

    var tandoorClient: TandoorClient? by mutableStateOf(defaultTandoorClient)

    val favorites = FavoritesViewModel()
    val settings = SettingsViewModel()

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

        viewModelScope.launch {
            if(settings.getFirstRunTime.first() == -1L)
                settings.setFirstRunTime()

            val credentials = settings.getTandoorCredentials.first()
            if(onBeforeCredentialsCheck(credentials)) return@launch

            if(credentials == null) {
                navHostController!!.navigate("onboarding") {
                    popUpTo("main") {
                        inclusive = true
                    }
                }
                return@launch
            }

            if(tandoorClient == null) tandoorClient = TandoorClient(credentials)
            favorites.init(tandoorClient!!)

            connectivityCheck()

            try {
                tandoorClient!!.serverSettings.current()
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

    // also deletes current ViewModel
    fun resetApp() {
        viewModelScope.launch {
            uiState.blockUI = true
            delay(250)

            clearForeverRememberNotSavable()
            clearForeverRememberMutableStateList()
            clearRememberAlternateNavController()

            uiState.deleteViewModel = true
        }
    }

    // enable offline state when having connectivity issues
    fun connectivityCheck() {
        if(tandoorClient == null) return
        if(!uiState.isInForeground) return

        viewModelScope.launch {
            var isOffline = true

            try {
                val response = tandoorClient!!.reqAny(
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
                    val response = tandoorClient!!.reqAny(
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

    fun signIn(client: TandoorClient, credentials: TandoorCredentials) {
        tandoorClient = client

        settings.saveTandoorCredentials(credentials)
        navHostController?.navigate("onboarding/welcome")

        favorites.init(tandoorClient!!)
        connectivityCheck()
    }

    fun signOut() {
        settings.setOnboardingCompleted(false)
        settings.saveTandoorCredentials(null)

        resetApp()
    }

}