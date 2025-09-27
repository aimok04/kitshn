package de.kitshn.ui.dialog.recipe.import

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

@Composable
actual fun rememberCameraPickerLauncherIfAvailable(
    onResult: (PlatformFile?) -> Unit
): () -> Unit {
    return { }
}