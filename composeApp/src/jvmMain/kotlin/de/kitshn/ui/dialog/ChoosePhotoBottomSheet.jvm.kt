package de.kitshn.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun ChoosePhotoBottomSheet(
    shown: Boolean,
    onSelect: (uri: ByteArray) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val launcher = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        onDismiss()

        coroutineScope.launch {
            file?.readBytes()?.let { onSelect(it) }
        }
    }

    LaunchedEffect(shown) {
        if(!shown) return@LaunchedEffect
        launcher.launch()
    }
}