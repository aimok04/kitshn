package de.kitshn.model.form.item

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

abstract class KitshnFormBaseItem {

    var generalError by mutableStateOf<String?>(null)

    @Composable
    open fun Render(
        modifier: Modifier
    ) {
    }

    open suspend fun submit(): Boolean {
        return true
    }

}