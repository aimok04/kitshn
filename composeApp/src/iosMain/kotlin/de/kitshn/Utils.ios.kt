package de.kitshn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalUriHandler
import platform.Foundation.NSString
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.posix.exit

@Composable
actual fun osDependentHapticFeedbackHandler(): ((type: HapticFeedbackType) -> Unit)? = null

@Composable
actual fun kotlinx.datetime.LocalDate.toHumanReadableDateLabelImpl(): String? = null

@Composable
actual fun BackHandler(enabled: Boolean, handler: () -> Unit) {
}

@Composable
actual fun KeepScreenOn() {
    DisposableEffect(Unit) {
        UIApplication.sharedApplication().idleTimerDisabled = true

        onDispose {
            UIApplication.sharedApplication().idleTimerDisabled = false
        }
    }
}

@Composable
actual fun launchMarketPageHandler(): () -> Unit {
    return {
        throw Exception("UTILS_IOS_launchMarketPageHandler_NOT_IMPLEMENTED")
    }
}

@Composable
actual fun launchWebsiteHandler(): (url: String) -> Unit {
    val uriHandler = LocalUriHandler.current

    return {
        uriHandler.openUri(it)
    }
}

@Composable
actual fun launchTimerHandler(): (seconds: Int, name: String) -> Unit {
    return { seconds, name ->
        throw Exception("CURRENTLY_NOT_SUPPORTED_IOS")
    }
}

@Composable
actual fun shareContentHandler(): (title: String, text: String) -> Unit {
    return { _, text ->
        val activityItems = listOf(NSString.create(string = text))
        val activityViewController =
            UIActivityViewController(activityItems = activityItems, applicationActivities = null)

        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootViewController?.presentViewController(
            activityViewController,
            animated = true,
            completion = null
        )
    }
}

@Composable
actual fun closeAppHandler(): () -> Unit {
    return {
        exit(0)
    }
}