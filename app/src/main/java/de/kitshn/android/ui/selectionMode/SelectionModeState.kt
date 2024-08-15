package de.kitshn.android.ui.selectionMode

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

@Composable
fun <T> rememberSelectionModeState(): SelectionModeState<T> {
    val state = remember { SelectionModeState<T>() }

    BackHandler(state.isSelectionModeEnabledState()) {
        state.disable()
    }

    return state
}

class SelectionModeState<T> {

    val selectedItems = mutableStateListOf<T>()

    @Composable
    fun isSelectionModeEnabledState(): Boolean {
        return selectedItems.size > 0
    }

    fun isSelectionModeEnabled(): Boolean {
        return selectedItems.size > 0
    }

    fun select(item: T) {
        if(selectedItems.contains(item)) return
        selectedItems.add(item)
    }

    fun deselect(item: T) {
        if(!selectedItems.contains(item)) return
        selectedItems.remove(item)
    }

    fun selectToggle(mealPlan: T) {
        if(selectedItems.contains(mealPlan)) {
            selectedItems.remove(mealPlan)
        } else {
            selectedItems.add(mealPlan)
        }
    }

    fun disable() {
        selectedItems.clear()
    }

}