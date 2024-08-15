package de.kitshn.android.ui.state

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * Static field, contains all scroll values
 */
private val SaveMap = mutableMapOf<String, KeyParamsScrollState>()

internal data class KeyParamsScrollState(
    val params: String = "",
    val initial: Int
)

/**
 * Save scroll state on all time.
 * @param key value for comparing screen
 * @param params arguments for find different between equals screen
 */
@Composable
fun rememberForeverScrollState(
    key: String,
    params: String = "",
    initial: Int = 0
): ScrollState {
    val scrollState = rememberSaveable(saver = ScrollState.Saver) {
        var savedValue = SaveMap[key]
        if(savedValue?.params != params) savedValue = null

        ScrollState(
            initial = savedValue?.initial ?: initial
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            SaveMap[key] = KeyParamsScrollState(
                params,
                scrollState.value
            )
        }
    }
    return scrollState
}