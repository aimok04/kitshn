package de.kitshn.ui.component.model.recipe.step

import androidx.compose.runtime.Composable
import de.kitshn.api.tandoor.model.TandoorStep

@Composable
actual fun isVideoSupported(): Boolean {
    return false
}

@Composable
actual fun VideoDialog(
    onDismiss: () -> Unit,
    step: TandoorStep
) {
}