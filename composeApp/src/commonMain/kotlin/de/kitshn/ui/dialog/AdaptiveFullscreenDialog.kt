package de.kitshn.ui.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
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
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.kitshn.BackHandler
import de.kitshn.ui.component.buttons.BackButton
import de.kitshn.ui.component.buttons.BackButtonType

@Composable
fun AdaptiveFullscreenDialog(
    onDismiss: () -> Unit,
    onPreDismiss: () -> Boolean = { true },
    forceDismiss: Boolean = false,
    title: @Composable () -> Unit = {},
    topAppBarActions: @Composable (RowScope.() -> Unit) = { },
    actions: @Composable (RowScope.() -> Unit)? = null,
    topBar: @Composable (() -> Unit)? = null,
    topBarWrapper: @Composable (topBar: @Composable () -> Unit) -> Unit = { it() },
    bottomBar: @Composable ((isFullscreen: Boolean) -> Unit)? = null,
    applyPaddingValues: Boolean = true,
    content: @Composable (nestedScrollConnection: NestedScrollConnection, isFullscreen: Boolean, pv: PaddingValues) -> Unit
) {
    if(adaptiveFullscreenDialogImpl(
            onDismiss = onDismiss,
            onPreDismiss = onPreDismiss,
            forceDismiss = forceDismiss,
            title = title,
            topAppBarActions = topAppBarActions,
            actions = actions,
            topBar = topBar,
            topBarWrapper = topBarWrapper,
            bottomBar = bottomBar,
            applyPaddingValues = applyPaddingValues,
            content = content
        )
    ) return

    CommonAdaptiveFullscreenDialog(
        onDismiss = onDismiss,
        onPreDismiss = onPreDismiss,
        forceDismiss = forceDismiss,
        title = title,
        topAppBarActions = topAppBarActions,
        actions = actions,
        topBar = topBar,
        topBarWrapper = topBarWrapper,
        bottomBar = bottomBar,
        applyPaddingValues = applyPaddingValues,
        content = content
    )
}

@Composable
expect fun adaptiveFullscreenDialogImpl(
    onDismiss: () -> Unit,
    onPreDismiss: () -> Boolean = { true },
    forceDismiss: Boolean = false,
    title: @Composable () -> Unit = {},
    topAppBarActions: @Composable (RowScope.() -> Unit) = { },
    actions: @Composable (RowScope.() -> Unit)? = null,
    topBar: @Composable (() -> Unit)? = null,
    topBarWrapper: @Composable (topBar: @Composable () -> Unit) -> Unit = { it() },
    bottomBar: @Composable ((isFullscreen: Boolean) -> Unit)? = null,
    applyPaddingValues: Boolean = true,
    content: @Composable (nestedScrollConnection: NestedScrollConnection, isFullscreen: Boolean, pv: PaddingValues) -> Unit
): Boolean

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonAdaptiveFullscreenDialog(
    onDismiss: () -> Unit,
    onPreDismiss: () -> Boolean = { true },
    forceDismiss: Boolean = false,
    title: @Composable () -> Unit = {},
    topAppBarActions: @Composable (RowScope.() -> Unit) = { },
    actions: @Composable (RowScope.() -> Unit)? = null,
    topBar: @Composable (() -> Unit)? = null,
    topBarWrapper: @Composable (topBar: @Composable () -> Unit) -> Unit = { it() },
    bottomBar: @Composable ((isFullscreen: Boolean) -> Unit)? = null,
    applyPaddingValues: Boolean = true,
    content: @Composable (nestedScrollConnection: NestedScrollConnection, isFullscreen: Boolean, pv: PaddingValues) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val fullscreenDialog = false

    val containerColor = if(fullscreenDialog) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    /* animate visibility of fullscreen dialog */
    var dismissOnPostExit by remember { mutableStateOf(false) }
    val animVisibleState = remember {
        MutableTransitionState(!fullscreenDialog)
            .apply { targetState = true }
    }

    /* dismiss fullscreen dialog after animation */
    if(!animVisibleState.targetState && !animVisibleState.currentState) {
        if(dismissOnPostExit) {
            onDismiss()
            return
        }
    }

    fun dismiss() {
        if(!onPreDismiss()) return

        if(fullscreenDialog) {
            dismissOnPostExit = true
            animVisibleState.targetState = false
        } else {
            onDismiss()
        }
    }

    LaunchedEffect(forceDismiss) { if(forceDismiss) dismiss() }
    BackHandler { dismiss() }

    Dialog(
        onDismissRequest = { dismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = fullscreenDialog
        )
    ) {
        AnimatedVisibility(
            animVisibleState
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
                                onBack = { dismiss() },
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
                            if(applyPaddingValues) {
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
    }
}