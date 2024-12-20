package de.kitshn.model.form.item

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

abstract class KitshnFormBaseItem {

    var generalError by mutableStateOf<String?>(null)

    @Composable
    open fun Render() {
    }

    open fun submit(): Boolean {
        return true
    }

}