package de.kitshn.ui.dialog

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection

@Composable
actual fun immersiveFullscreenDialogImpl(
    onDismiss: () -> Unit,
    onPreDismiss: () -> Boolean,
    forceDismiss: Boolean,
    title: @Composable () -> Unit,
    topAppBarActions: @Composable (RowScope.() -> Unit),
    actions: @Composable (RowScope.() -> Unit)?,
    topBar: @Composable (() -> Unit)?,
    topBarWrapper: @Composable (topBar: @Composable () -> Unit) -> Unit,
    bottomBar: @Composable (() -> Unit)?,
    applyPaddingValues: Boolean,
    content: @Composable (nestedScrollConnection: NestedScrollConnection, pv: PaddingValues) -> Unit
): Boolean {
    return false
}