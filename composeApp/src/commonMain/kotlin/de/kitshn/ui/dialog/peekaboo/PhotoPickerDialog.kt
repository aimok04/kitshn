package de.kitshn.ui.dialog.peekaboo

import androidx.compose.runtime.Composable

@Composable
fun PhotoPickerDialog(
    shown: Boolean,
    onSelect: (uri: ByteArray) -> Unit,
    onDismiss: () -> Unit
) {
    if(photoPickerDialogImpl(
            shown = shown,
            onSelect = onSelect,
            onDismiss = onDismiss
        )
    ) return
}

@Composable
expect fun photoPickerDialogImpl(
    shown: Boolean,
    onSelect: (uri: ByteArray) -> Unit,
    onDismiss: () -> Unit
): Boolean