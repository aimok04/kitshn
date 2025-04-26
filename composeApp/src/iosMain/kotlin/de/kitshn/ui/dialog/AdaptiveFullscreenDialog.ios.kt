package de.kitshn.ui.dialog

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
actual fun AdaptiveFullscreenDialog(
    onDismiss: () -> Unit,
    onPreDismiss: () -> Boolean,
    forceDismiss: Boolean,
    title: @Composable () -> Unit,
    topAppBarActions: @Composable (RowScope.() -> Unit),
    actions: @Composable (RowScope.() -> Unit)?,
    topBar: @Composable (() -> Unit)?,
    topBarWrapper: @Composable (topBar: @Composable () -> Unit) -> Unit,
    bottomBar: @Composable ((isFullscreen: Boolean) -> Unit)?,
    forceFullscreen: Boolean,
    forceDialog: Boolean,
    disableAnimation: Boolean,
    maxWidth: Dp,
    applyPaddingValues: Boolean,
    content: @Composable (nestedScrollConnection: NestedScrollConnection, isFullscreen: Boolean, pv: PaddingValues) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isFullscreen = if(forceFullscreen || forceDialog) {
        forceFullscreen
    } else {
        (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT)
                || (windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT)
    }

    val containerColor = if(isFullscreen) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    LaunchedEffect(forceDismiss) { if(forceDismiss) onDismiss() }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            usePlatformInsets = !isFullscreen
        )
    ) {
        CommonAdaptiveFullscreenDialogContent(
            containerColor = containerColor,
            isFullscreen = isFullscreen,
            scrollBehavior = scrollBehavior,

            title = title,
            topAppBarActions = topAppBarActions,
            actions = actions,
            topBar = topBar,
            topBarWrapper = topBarWrapper,
            bottomBar = bottomBar,

            maxWidth = maxWidth,
            applyPaddingValues = applyPaddingValues,
            onDismiss = onDismiss,
            content = content
        )
    }
}