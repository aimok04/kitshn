package de.kitshn.ui.dialog.external

import androidx.compose.runtime.Composable

@Composable
actual fun photoPickerDialogImpl(
    shown: Boolean,
    onSelect: (image: ByteArray) -> Unit,
    onDismiss: () -> Unit
): Boolean {
    return false
}