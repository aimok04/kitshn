import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitViewController
import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Logger
import co.touchlab.kermit.NSLogWriter
import co.touchlab.kermit.OSLogWriter
import co.touchlab.kermit.bugsnag.BugsnagLogWriter
import de.kitshn.App
import de.kitshn.KitshnViewModel
import de.kitshn.actions.handlers.handleAppLink
import de.kitshn.ui.component.buttons.BackButton
import de.kitshn.ui.component.buttons.BackButtonType
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.allStringResources
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getString
import platform.UIKit.UIViewController

var deepLinkUrl by mutableStateOf("")
var mIsSubscribed by mutableStateOf(false)

@OptIn(ExperimentalKermitApi::class, ExperimentalComposeUiApi::class)
fun MainViewController(
    subscriptionUI: () -> UIViewController
): UIViewController = ComposeUIViewController(
    configure = {
        enableBackGesture = false
    }
) {
    Logger.setLogWriters(OSLogWriter(), NSLogWriter(), BugsnagLogWriter())

    var vm by remember { mutableStateOf<KitshnViewModel?>(null) }

    App(
        onVmCreated = {
            vm = it

            it.uiState.iosIsSubscribed = mIsSubscribed
            it.manageIosSubscriptionView = { p ->
                Box(
                    Modifier.fillMaxSize()
                ) {
                    UIKitViewController(
                        modifier = Modifier.fillMaxSize(),
                        factory = subscriptionUI
                    )

                    Box(
                        Modifier.windowInsetsPadding(WindowInsets.systemBars)
                            .padding(16.dp)
                    ) {
                        BackButton(
                            p.onBack,
                            overlay = true,
                            type = BackButtonType.CLOSE
                        )
                    }
                }
            }
        },
        onBeforeCredentialsCheck = { credentials ->
            if (deepLinkUrl.isNotEmpty()) {
                vm?.handleAppLink(credentials, deepLinkUrl) ?: false
            } else {
                false
            }
        },
        onLaunched = {
            if (deepLinkUrl.isEmpty()) return@App
            vm?.handleAppLink(deepLinkUrl)
        }
    )

    LaunchedEffect(deepLinkUrl) {
        if (deepLinkUrl.isEmpty()) return@LaunchedEffect
        vm?.handleAppLink(deepLinkUrl)
    }

    LaunchedEffect(mIsSubscribed) {
        vm?.uiState?.iosIsSubscribed = mIsSubscribed
    }
}

fun handleSubscriptionChange(isSubscribed: Boolean) {
    mIsSubscribed = isSubscribed
}

fun handleDeepLink(url: String?) {
    if (url == null) return
    deepLinkUrl = url
}

@OptIn(ExperimentalResourceApi::class)
fun lang(key: String): String {
    return runBlocking {
        Res.allStringResources[key]?.let { getString(it) } ?: "UNDEFINED"
    }
}