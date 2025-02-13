package de.kitshn.ui.dialog

import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import de.kitshn.getActivityWindow
import de.kitshn.getDialogWindow

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
    val configuration = LocalConfiguration.current
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

    /* animate visibility of fullscreen dialog */
    var dismissOnPostExit by remember { mutableStateOf(false) }
    val animVisibilityState = remember {
        MutableTransitionState(!isFullscreen)
            .apply { targetState = true }
    }

    /* dismiss fullscreen dialog after animation */
    if(!animVisibilityState.targetState && !animVisibilityState.currentState) {
        if(dismissOnPostExit) {
            onDismiss()
            return
        }
    }

    fun dismiss() {
        if(!onPreDismiss()) return

        if(isFullscreen) {
            dismissOnPostExit = true
            animVisibilityState.targetState = false
        } else {
            onDismiss()
        }
    }

    LaunchedEffect(forceDismiss) { if(forceDismiss) dismiss() }
    BackHandler { dismiss() }

    Dialog(
        onDismissRequest = { dismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = isFullscreen,
            decorFitsSystemWindows = false
        )
    ) {
        if(isFullscreen) {
            val activityWindow = getActivityWindow()
            val dialogWindow = getDialogWindow()
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

            if(MaterialTheme.colorScheme.background.luminance() > 0.8f) {
                val systemUiController = rememberSystemUiController(activityWindow)
                val dialogSystemUiController = rememberSystemUiController(dialogWindow)

                LaunchedEffect(configuration) {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = true
                    )
                    dialogSystemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = true
                    )
                }
            }
        }

        AnimatedVisibility(
            animVisibilityState
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
                onDismiss = { dismiss() },
                content = content
            )
        }
    }
}