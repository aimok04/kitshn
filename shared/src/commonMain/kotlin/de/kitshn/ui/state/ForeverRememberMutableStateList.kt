package de.kitshn.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Static field, contains all scroll values
 */
private val SaveMap = mutableMapOf<String, KeyParamsMutableStateList>()

private data class KeyParamsMutableStateList(
    val params: String = "",
    val list: List<Any>
)

/**
 * Save int state on all time.
 * @param key value for comparing screen
 * @param params arguments for find different between equals screen
 */
@Composable
fun <T> foreverRememberMutableStateList(
    key: String,
    params: String = ""
): SnapshotStateList<T> {
    val mutableState = remember {
        var savedValue = SaveMap[key]
        if(savedValue?.params != params) savedValue = null

        val list = mutableStateListOf<T>()
        if(savedValue?.list != null) list.addAll(savedValue.list as List<T>)

        list
    }
    DisposableEffect(Unit) {
        onDispose {
            SaveMap[key] = KeyParamsMutableStateList(params, mutableState.toList() as List<Any>)
        }
    }
    return mutableState
}

fun clearForeverRememberMutableStateList() {
    SaveMap.clear()
}