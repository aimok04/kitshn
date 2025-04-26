package de.kitshn.ui.dialog.peekaboo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import kotlinx.coroutines.delay

@Composable
actual fun photoPickerDialogImpl(
    shown: Boolean,
    onSelect: (image: ByteArray) -> Unit,
    onDismiss: () -> Unit
): Boolean {
    val scope = rememberCoroutineScope()

    val singleImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            byteArrays.firstOrNull()?.let {
                onSelect(it)
            }

            onDismiss()
        }
    )

    LaunchedEffect(shown) {
        if(!shown) return@LaunchedEffect
        singleImagePicker.launch()

        // dirty fix
        delay(500)
        onDismiss()
    }

    return true
}