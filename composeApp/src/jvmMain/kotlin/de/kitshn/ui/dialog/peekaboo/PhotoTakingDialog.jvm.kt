package de.kitshn.ui.dialog.peekaboo

import androidx.compose.runtime.Composable

@Composable
actual fun photoTakingDialogImpl(
    shown: Boolean,
    onSelect: (image: ByteArray) -> Unit,
    onDismiss: () -> Unit
): Boolean {
    return false
}