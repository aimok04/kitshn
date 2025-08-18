package de.kitshn.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.kitshn.ui.component.buttons.BackButton
import de.kitshn.ui.component.buttons.BackButtonType

@Composable
expect fun AdaptiveFullscreenDialog(
    onDismiss: () -> Unit,
    onPreDismiss: () -> Boolean = { true },
    forceDismiss: Boolean = false,
    title: @Composable () -> Unit = {},
    topAppBarActions: @Composable (RowScope.() -> Unit) = { },
    actions: @Composable (RowScope.() -> Unit)? = null,
    topBar: @Composable (() -> Unit)? = null,
    topBarWrapper: @Composable (topBar: @Composable () -> Unit) -> Unit = { it() },
    bottomBar: @Composable ((isFullscreen: Boolean) -> Unit)? = null,
    forceFullscreen: Boolean = false,
    forceDialog: Boolean = false,
    disableAnimation: Boolean = false,
    maxWidth: Dp = 900.dp,
    applyPaddingValues: Boolean = true,
    content: @Composable (nestedScrollConnection: NestedScrollConnection, isFullscreen: Boolean, pv: PaddingValues) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonAdaptiveFullscreenDialogContent(
    containerColor: Color,
    isFullscreen: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,

    title: @Composable () -> Unit = {},
    topAppBarActions: @Composable (RowScope.() -> Unit) = { },
    actions: @Composable (RowScope.() -> Unit)? = null,
    topBar: @Composable (() -> Unit)? = null,
    topBarWrapper: @Composable (topBar: @Composable () -> Unit) -> Unit = { it() },
    bottomBar: @Composable ((isFullscreen: Boolean) -> Unit)? = null,

    maxWidth: Dp = 900.dp,
    applyPaddingValues: Boolean = true,
    onDismiss: () -> Unit,
    content: @Composable (nestedScrollConnection: NestedScrollConnection, isFullscreen: Boolean, pv: PaddingValues) -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        // give haptic feedback when opening a fullscreen dialog
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    // detect clicks around Surface padding to close dialog
    Box(
        Modifier
            .padding(40.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onDismiss()
            }
    ) {
        Surface(
            Modifier
                .widthIn(
                    max = if(isFullscreen) {
                        Dp.Unspecified
                    } else {
                        maxWidth
                    }
                )
                .then(
                    when(isFullscreen) {
                        true -> Modifier.fillMaxSize()
                        else -> Modifier.padding(40.dp)
                    }
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { }
                .imePadding(),
            color = containerColor,
            shape = if(isFullscreen) {
                RectangleShape
            } else {
                AlertDialogDefaults.shape
            }
        ) {
            val mTopBar = topBar ?: {
                CenterAlignedTopAppBar(
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
                    windowInsets = when(isFullscreen) {
                        true -> TopAppBarDefaults.windowInsets
                        else -> WindowInsets()
                    },
                    scrollBehavior = scrollBehavior
                )
            }

            val internalBottomBar = @Composable {
                if(bottomBar != null) {
                    bottomBar(isFullscreen)
                } else if(actions != null) {
                    BottomAppBar(
                        actions = {},
                        floatingActionButton = {
                            Row {
                                actions()
                            }
                        },
                        windowInsets = when(isFullscreen) {
                            true -> BottomAppBarDefaults.windowInsets
                            else -> WindowInsets()
                        }
                    )
                }
            }

            if(isFullscreen) {
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