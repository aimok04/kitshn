package de.kitshn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.ui.component.shopping.AdditionalShoppingSettingsChipRowState

class AppOfflineState {
    var isOffline by mutableStateOf(false)
}

class UiStateLink<T> {

    private var value by mutableStateOf<T?>(null)

    fun set(data: T) {
        this.value = data
    }

    @Composable
    fun WatchAndConsume(callback: suspend (value: T) -> Unit) {
        LaunchedEffect(value) {
            if(value == null) return@LaunchedEffect

            callback(value!!)
            value = null
        }
    }

}

class UiStateModel : ViewModel() {

    var blockUI by mutableStateOf(false)
    var deleteViewModel by mutableStateOf(false)

    var isInForeground by mutableStateOf(true)

    var userDisplayName by mutableStateOf("")

    var offlineState = AppOfflineState()

    var importRecipeUrl = UiStateLink<String>()
    var searchKeyword = UiStateLink<Int>()
    var searchCreatedBy = UiStateLink<Int>()
    var viewRecipe = UiStateLink<Int>()

    var additionalShoppingSettingsChipRowState = AdditionalShoppingSettingsChipRowState()

    var shareClient: TandoorClient? = null

    var iosIsSubscribed by mutableStateOf(false)

}