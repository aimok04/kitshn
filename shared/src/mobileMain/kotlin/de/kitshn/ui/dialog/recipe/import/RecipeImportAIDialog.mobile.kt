package de.kitshn.ui.dialog.recipe.import

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher

@Composable
actual fun rememberCameraPickerLauncherIfAvailable(
    onResult: (PlatformFile?) -> Unit
): () -> Unit {
    val cameraPickerLauncher = rememberCameraPickerLauncher(
        onResult = onResult
    )

    return { cameraPickerLauncher.launch() }
}