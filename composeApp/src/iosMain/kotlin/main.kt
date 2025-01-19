import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.kermit.Logger
import co.touchlab.kermit.NSLogWriter
import co.touchlab.kermit.OSLogWriter
import de.kitshn.App
import de.kitshn.KitshnViewModel
import de.kitshn.actions.handlers.handleAppLink
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.allStringResources
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getString
import platform.UIKit.UIViewController

var deepLinkUrl by mutableStateOf("")

fun MainViewController(): UIViewController = ComposeUIViewController {
    Logger.setLogWriters(OSLogWriter(), NSLogWriter())

    var vm by remember { mutableStateOf<KitshnViewModel?>(null) }

    App(
        onVmCreated = {
            vm = it
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