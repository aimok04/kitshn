package de.kitshn.ui.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import de.kitshn.ui.component.buttons.BackButton
import de.kitshn.ui.component.buttons.BackButtonType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
actual fun adaptiveFullscreenDialogImpl(
    onDismiss: () -> Unit,
    onPreDismiss: () -> Boolean,
    forceDismiss: Boolean,
    title: @Composable () -> Unit,
    topAppBarActions: @Composable (RowScope.() -> Unit),
    actions: @Composable (RowScope.() -> Unit)?,
    topBar: @Composable (() -> Unit)?,
    topBarWrapper: @Composable (topBar: @Composable () -> Unit) -> Unit,
    bottomBar: @Composable ((isFullscreen: Boolean) -> Unit)?,
    applyPaddingValues: Boolean,
    content: @Composable (nestedScrollConnection: NestedScrollConnection, isFullscreen: Boolean, pv: PaddingValues) -> Unit
): Boolean {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val fullscreenDialog = (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT)
            || (windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT)

    val containerColor = if(fullscreenDialog) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    LaunchedEffect(forceDismiss) { if(forceDismiss) onDismiss() }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            usePlatformInsets = !fullscreenDialog
        )
    ) {
        Surface(
            Modifier
                .widthIn(
                    max = if(fullscreenDialog) {
                        Dp.Unspecified
                    } else {
                        900.dp
                    }
                )
                .run {
                    if(fullscreenDialog) {
                        this.fillMaxSize()
                    } else {
                        this.padding(40.dp)
                    }
                },
            color = containerColor,
            shape = if(fullscreenDialog) {
                RectangleShape
            } else {
                AlertDialogDefaults.shape
            }
        ) {
            val mTopBar = topBar ?: {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = containerColor
                    ),
                    navigationIcon = {
                        BackButton(
                            onBack = { onDismiss() },
                            type = BackButtonType.CLOSE
                        )
                    },
                    title = title,
                    actions = topAppBarActions,
                    scrollBehavior = scrollBehavior
                )
            }

            val internalBottomBar = @Composable {
                if(bottomBar != null) {
                    bottomBar(fullscreenDialog)
                } else if(actions != null) BottomAppBar(
                    containerColor = containerColor,
                    actions = {},
                    floatingActionButton = {
                        Row {
                            actions()
                        }
                    }
                )
            }

            if(fullscreenDialog) {
                Scaffold(
                    topBar = { topBarWrapper(mTopBar) },
                    bottomBar = internalBottomBar,
                    containerColor = containerColor
                ) {
                    Box(
                        if(applyPaddingValues && fullscreenDialog) {
                            Modifier.padding(it)
                        } else {
                            Modifier
                        }
                    ) {
                        content(scrollBehavior.nestedScrollConnection, true, it)
                    }
                }
            } else {
                Column {
                    topBarWrapper(mTopBar)

                    BoxWithConstraints {
                        val maxHeight = this.maxHeight

                        Column {
                            Box(
                                Modifier.heightIn(max = maxHeight - 80.dp)
                            ) {
                                content(
                                    scrollBehavior.nestedScrollConnection,
                                    false,
                                    PaddingValues()
                                )
                            }

                            internalBottomBar()
                        }
                    }
                }
            }
        }
    }

    return true
}