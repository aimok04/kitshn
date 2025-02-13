package de.kitshn.ui.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.kitshn.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
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
    maxWidth: Dp,
    applyPaddingValues: Boolean,
    content: @Composable (nestedScrollConnection: NestedScrollConnection, isFullscreen: Boolean, pv: PaddingValues) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val containerColor = MaterialTheme.colorScheme.surface

    val animVisibilityState = remember { MutableTransitionState(true) }

    fun dismiss() {
        if(!onPreDismiss()) return
        onDismiss()
    }

    LaunchedEffect(forceDismiss) { if(forceDismiss) dismiss() }
    BackHandler { dismiss() }

    Dialog(
        onDismissRequest = { dismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        AnimatedVisibility(
            animVisibilityState
        ) {
            CommonAdaptiveFullscreenDialogContent(
                containerColor = containerColor,
                isFullscreen = false,
                scrollBehavior = scrollBehavior,

                title = title,
                topAppBarActions = topAppBarActions,
                actions = actions,
                topBar = topBar,
                topBarWrapper = topBarWrapper,
                bottomBar = bottomBar,

                maxWidth = maxWidth,
                applyPaddingValues = applyPaddingValues,
                onDismiss = { dismiss() },
                content = content
            )
        }
    }
}