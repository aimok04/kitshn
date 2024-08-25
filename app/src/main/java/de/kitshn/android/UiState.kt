package de.kitshn.android

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import de.kitshn.android.api.tandoor.TandoorClient

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

    var isInForeground by mutableStateOf(false)

    var importRecipeUrl = UiStateLink<String>()
    var searchKeyword = UiStateLink<Int>()
    var viewRecipe = UiStateLink<Int>()

    var shareClient: TandoorClient? = null

}