package de.kitshn.android.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * Static field, contains all scroll values
 */
private val SaveMap = mutableMapOf<String, KeyParamsIntState>()

private data class KeyParamsIntState(
    val params: String = "",
    val value: Int
)

/**
 * Save int state on all time.
 * @param key value for comparing screen
 * @param params arguments for find different between equals screen
 * @param initialValue initial value for rememberSavable
 */
@Composable
fun foreverRememberIntState(
    key: String,
    params: String = "",
    initialValue: Int = 0
): MutableIntState {
    val mutableIntState = rememberSaveable {
        var savedValue = SaveMap[key]
        if(savedValue?.params != params) savedValue = null

        mutableIntStateOf(savedValue?.value ?: initialValue)
    }
    DisposableEffect(Unit) {
        onDispose {
            SaveMap[key] = KeyParamsIntState(params, mutableIntState.intValue)
        }
    }
    return mutableIntState
}