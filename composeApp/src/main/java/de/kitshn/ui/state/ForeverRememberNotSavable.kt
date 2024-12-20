package de.kitshn.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

/**
 * Static field, contains all scroll values
 */
private val SaveMap = mutableMapOf<String, KeyParamsAnyStateNotSavable>()

private data class KeyParamsAnyStateNotSavable(
    val params: String = "",
    val value: Any
)

/**
 * Save int state on all time.
 * @param key value for comparing screen
 * @param params arguments for find different between equals screen
 * @param initialValue initial value for rememberSavable
 */
@Composable
fun <T> foreverRememberNotSavable(
    key: String,
    params: String = "",
    initialValue: T? = null
): MutableState<T> {
    val mutableState = remember {
        var savedValue = SaveMap[key]
        if(savedValue?.params != params) savedValue = null

        mutableStateOf((savedValue?.value ?: initialValue) as T)
    }
    DisposableEffect(Unit) {
        onDispose {
            if(mutableState.value == null) return@onDispose
            SaveMap[key] = KeyParamsAnyStateNotSavable(params, mutableState.value as Any)
        }
    }
    return mutableState
}