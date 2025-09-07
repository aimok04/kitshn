package de.kitshn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.intl.Locale
import co.touchlab.crashkios.bugsnag.BugsnagKotlin
import com.eygraber.uri.Uri
import de.kitshn.ui.dialog.LaunchTimerInfoBottomSheetState
import de.kitshn.ui.dialog.LaunchTimerRangeBottomSheetState
import kitshn.composeApp.BuildConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.popoverPresentationController
import platform.posix.exit

actual fun saveBreadcrumb(key: String, value: String) {
    BugsnagKotlin.setCustomValue(section = "kotlin", key = key, value = value)
}

@Composable
actual fun LocalDate.format(pattern: String): String = NSDateFormatter().run {
    setLocale(Locale.current.platformLocale)
    setLocalizedDateFormatFromTemplate(pattern)
    stringFromDate(this@format.toNSDate())
}

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
    val uriHandler = LocalUriHandler.current

    return {
        uriHandler.openUri(BuildConfig.ABOUT_APPLE_APPSTORE)
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
actual fun launchTimerHandler(
    vm: KitshnViewModel,
    infoBottomSheetState: LaunchTimerInfoBottomSheetState,
    rangeBottomSheetState: LaunchTimerRangeBottomSheetState
): (fromSeconds: Int, toSeconds: Int, name: String) -> Unit {
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    fun startTimer(seconds: Int) {
        coroutineScope.launch {
            val isShortcutInstalled = vm.settings.getIosTimerShortcutInstalled.first()
            if (!isShortcutInstalled) {
                infoBottomSheetState.open()
                return@launch
            }

            val builder = Uri.parse("shortcuts://run-shortcut")
                .buildUpon()
                .appendQueryParameter("name", BuildConfig.IOS_TIMER_SHORTCUT_NAME)
                .appendQueryParameter("input", "text")
                .appendQueryParameter("text", seconds.toString())

            uriHandler.openUri(builder.build().toString())
        }
    }

    return handler@{ fromSeconds, toSeconds, name ->
        if(fromSeconds == toSeconds) {
            startTimer(fromSeconds)
            return@handler
        }

        rangeBottomSheetState.open(
            from = fromSeconds,
            to = toSeconds
        ) {
            startTimer(it)
        }
    }
}

actual val isLaunchTimerHandlerImplemented = true

@Composable
actual fun shareContentHandler(): (title: String, text: String) -> Unit {
    return { title, text ->
        val activityController = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null,
        )

        val window = UIApplication.sharedApplication.windows().first() as UIWindow?
        activityController.popoverPresentationController()?.sourceView =
            window
        activityController.setTitle(title)

        window?.rootViewController?.presentViewController(
            activityController as UIViewController,
            animated = true,
            completion = null,
        )
    }
}

@Composable
actual fun closeAppHandler(): () -> Unit {
    return {
        exit(0)
    }
}

/**
 * Transforms kotlin LocalDate to iOS NSDate
 *
 */
fun LocalDate.toNSDate(): NSDate {
    val referenceDateDays = this.toEpochDays() - 31 * 365 - 8
    return NSDate((referenceDateDays * 24 * 60 * 60).toDouble())
}