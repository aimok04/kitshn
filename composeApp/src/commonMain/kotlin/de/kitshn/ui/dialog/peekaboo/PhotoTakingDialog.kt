package de.kitshn.ui.dialog.peekaboo

import androidx.compose.runtime.Composable

@Composable
fun PhotoTakingDialog(
    shown: Boolean,
    onSelect: (uri: ByteArray) -> Unit,
    onDismiss: () -> Unit
) {
    if(photoTakingDialogImpl(
            shown = shown,
            onSelect = onSelect,
            onDismiss = onDismiss
        )
    ) return
}

@Composable
expect fun photoTakingDialogImpl(
    shown: Boolean,
    onSelect: (uri: ByteArray) -> Unit,
    onDismiss: () -> Unit
): Boolean