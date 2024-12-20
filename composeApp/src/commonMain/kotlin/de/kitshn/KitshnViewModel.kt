package de.kitshn

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import de.kitshn.actions.handleIntent
import de.kitshn.actions.preHandleIntent
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestsError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
class KitshnViewModel(
    app: Application,
    val context: Context,
    val intent: Intent,
    defaultTandoorClient: TandoorClient? = null
) : AndroidViewModel(app) {

    var navHostController: NavHostController? = null
    var mainSubNavHostController: NavHostController? = null

    var tandoorClient: TandoorClient? by mutableStateOf(defaultTandoorClient)

    val favorites = FavoritesViewModel(app, context)
    val settings = SettingsViewModel(app, context)

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

    init {
        viewModelScope.launch {
            val credentials = settings.getTandoorCredentials.first()

            if(preHandleIntent(credentials, intent)) return@launch

            if(credentials == null) {
                navHostController?.navigate("onboarding") {
                    popUpTo("main") {
                        inclusive = true
                    }
                }
                return@launch
            }

            if(tandoorClient == null) tandoorClient = TandoorClient(context, credentials)
            favorites.init(tandoorClient!!)

            try {
                tandoorClient!!.openapi.get()
            } catch(e: TandoorRequestsError) {
                e.printStackTrace()
            } catch(e: Exception) {
                e.printStackTrace()
            }

            if(settings.getOnboardingCompleted.first()) {
                handleIntent(intent)
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