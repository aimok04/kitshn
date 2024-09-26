package de.kitshn.android.ui.dialog

import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import de.kitshn.android.getActivityWindow
import de.kitshn.android.getDialogWindow
import de.kitshn.android.ui.component.buttons.BackButton
import de.kitshn.android.ui.component.buttons.BackButtonType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImmersiveFullscreenDialog(
    onDismiss: () -> Unit,
    onPreDismiss: () -> Boolean = { true },
    forceDismiss: Boolean = false,
    title: @Composable () -> Unit = {},
    topAppBarActions: @Composable (RowScope.() -> Unit) = { },
    actions: @Composable (RowScope.() -> Unit)? = null,
    topBar: @Composable (() -> Unit)? = null,
    topBarWrapper: @Composable (topBar: @Composable () -> Unit) -> Unit = { it() },
    bottomBar: @Composable (() -> Unit)? = null,
    applyPaddingValues: Boolean = true,
    content: @Composable (nestedScrollConnection: NestedScrollConnection, pv: PaddingValues) -> Unit
) {
    val configuration = LocalConfiguration.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    /* animate visibility of fullscreen dialog */
    var dismissOnPostExit by remember { mutableStateOf(false) }
    val animVisibleState = remember {
        MutableTransitionState(false)
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

        dismissOnPostExit = true
        animVisibleState.targetState = false
    }

    LaunchedEffect(forceDismiss) { if(forceDismiss) dismiss() }
    BackHandler { dismiss() }

    Dialog(
        onDismissRequest = { dismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = true,
            decorFitsSystemWindows = false
        )
    ) {
        val activityWindow = getActivityWindow()
        val dialogWindow = getDialogWindow()

        val systemUiController = rememberSystemUiController(activityWindow)
        val dialogSystemUiController = rememberSystemUiController(dialogWindow)

        val parentView = LocalView.current.parent as View

        fun apply() {
            if(activityWindow != null && dialogWindow != null) {
                val attributes = WindowManager.LayoutParams()
                attributes.copyFrom(activityWindow.attributes)
                attributes.type = dialogWindow.attributes.type
                dialogWindow.attributes = attributes
                parentView.layoutParams = FrameLayout.LayoutParams(
                    activityWindow.decorView.width,
                    activityWindow.decorView.height
                )
            }
        }

        SideEffect { apply() }
        LaunchedEffect(configuration) { apply() }

        DisposableEffect(configuration) {
            systemUiController.isSystemBarsVisible = false
            dialogSystemUiController.isSystemBarsVisible = false

            onDispose {
                systemUiController.isSystemBarsVisible = true
                dialogSystemUiController.isSystemBarsVisible = true
            }
        }

        AnimatedVisibility(
            animVisibleState
        ) {
            Surface(
                Modifier
                    .fillMaxSize()
            ) {
                val mTopBar = topBar ?: {
                    TopAppBar(
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
                        bottomBar()
                    } else if(actions != null) BottomAppBar(
                        actions = {},
                        floatingActionButton = {
                            Row {
                                actions()
                            }
                        }
                    )
                }

                Scaffold(
                    topBar = { topBarWrapper(mTopBar) },
                    bottomBar = internalBottomBar,
                ) {
                    Box(
                        if(applyPaddingValues) {
                            Modifier.padding(it)
                        } else {
                            Modifier
                        }
                    ) {
                        content(scrollBehavior.nestedScrollConnection, it)
                    }
                }
            }
        }
    }
}