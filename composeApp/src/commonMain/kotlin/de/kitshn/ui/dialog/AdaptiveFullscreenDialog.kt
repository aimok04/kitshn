package de.kitshn.ui.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
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
    Surface(
        Modifier
            .widthIn(
                max = if(isFullscreen) {
                    Dp.Unspecified
                } else {
                    maxWidth
                }
            )
            .run {
                if(isFullscreen) {
                    this.fillMaxSize()
                } else {
                    this.padding(40.dp)
                }
            },
        color = containerColor,
        shape = if(isFullscreen) {
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
                bottomBar(isFullscreen)
            } else if(actions != null) {
                val yOffset = if(isFullscreen) {
                    -(WindowInsets.ime.asPaddingValues()
                        .calculateBottomPadding() - WindowInsets.systemBars.asPaddingValues()
                        .calculateBottomPadding())
                } else {
                    0.dp
                }

                BottomAppBar(
                    modifier = Modifier.offset(
                        y = yOffset.coerceAtMost(0.dp)
                    ),
                    actions = {},
                    floatingActionButton = {
                        Row {
                            actions()
                        }
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