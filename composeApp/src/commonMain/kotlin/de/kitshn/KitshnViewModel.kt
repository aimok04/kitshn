package de.kitshn

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorCredentials
import de.kitshn.api.tandoor.TandoorRequestsError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

    fun searchKeyword(id: Int) {
        viewModelScope.launch {
            navigateTo("main", "home")
            uiState.searchKeyword.set(id)
        }
    }

    fun viewRecipe(id: Int) {
        viewModelScope.launch {
            navigateTo("main", "home")
            uiState.viewRecipe.set(id)
        }
    }

    var initComplete = false

    fun init() {
        if(initComplete) return
        initComplete = true

        viewModelScope.launch {
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

            try {
                tandoorClient!!.openapi.get()
            } catch(e: TandoorRequestsError) {
                e.printStackTrace()
            } catch(e: Exception) {
                e.printStackTrace()
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

}