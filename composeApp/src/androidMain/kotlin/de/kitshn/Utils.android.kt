package de.kitshn

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.view.HapticFeedbackConstants
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import co.touchlab.kermit.Logger
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.recipe_step_timer_created
import kitshn.composeapp.generated.resources.recipe_step_timer_error_no_app
import kotlinx.coroutines.launch
import org.acra.ACRA
import org.jetbrains.compose.resources.getString
import java.time.LocalDate
import java.time.format.DateTimeFormatter

tailrec fun Context.getActivityWindow(): Window? =
    when(this) {
        is Activity -> window
        is ContextWrapper -> baseContext.getActivityWindow()
        else -> null
    }

@Composable
fun getDialogWindow(): Window? = (LocalView.current.parent as? DialogWindowProvider)?.window

@Composable
fun getActivityWindow(): Window? = LocalView.current.context.getActivityWindow()

actual fun saveBreadcrumb(key: String, value: String) {
    ACRA.errorReporter.putCustomData(key, value)
}

@Composable
actual fun osDependentHapticFeedbackHandler(): ((type: HapticFeedbackType) -> Unit)? {
    val view = LocalView.current

    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        {
            view.performHapticFeedback(
                when(it) {
                    HapticFeedbackType.SHORT_TICK -> HapticFeedbackConstants.SEGMENT_FREQUENT_TICK
                    HapticFeedbackType.DRAG_START -> HapticFeedbackConstants.DRAG_START
                    HapticFeedbackType.GESTURE_START -> HapticFeedbackConstants.GESTURE_START
                    HapticFeedbackType.GESTURE_END -> HapticFeedbackConstants.GESTURE_END
                    else -> HapticFeedbackConstants.LONG_PRESS
                }
            )
        }
    } else {
        null
    }
}

@Composable
actual fun kotlinx.datetime.LocalDate.format(pattern: String): String =
    DateTimeFormatter.ofPattern(pattern).format(LocalDate.ofEpochDay(this.toEpochDays().toLong()))

@Composable
actual fun BackHandler(enabled: Boolean, handler: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled, handler)
}

@Composable
actual fun KeepScreenOn() {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val window = context.getActivityWindow()
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

@Composable
actual fun launchMarketPageHandler(): () -> Unit {
    val context = LocalContext.current

    return {
        with(context) {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$packageName")
                    )
                )
            } catch(e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            }
        }
    }
}

@Composable
actual fun launchWebsiteHandler(): (url: String) -> Unit {
    val context = LocalContext.current

    return {
        CustomTabsIntent.Builder().build()
            .launchUrl(context, Uri.parse(it))
    }
}

@Composable
actual fun launchTimerHandler(): (seconds: Int, name: String) -> Unit {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    return { seconds, name ->
        try {
            context.startActivity(
                Intent().apply {
                    action = AlarmClock.ACTION_SET_TIMER
                    putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                    putExtra(AlarmClock.EXTRA_MESSAGE, name)
                    putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                }
            )

            coroutineScope.launch {
                Toast.makeText(
                    context,
                    getString(Res.string.recipe_step_timer_created), Toast.LENGTH_SHORT
                ).show()
            }
        } catch(e: ActivityNotFoundException) {
            Logger.e("Utils.android.kt", e)

            coroutineScope.launch {
                Toast.makeText(
                    context,
                    getString(Res.string.recipe_step_timer_error_no_app), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

actual val isLaunchTimerHandlerImplemented = true

@Composable
actual fun shareContentHandler(): (title: String, text: String) -> Unit {
    val context = LocalContext.current

    return { title, text ->
        context.startActivity(
            Intent.createChooser(Intent().apply {
                action = Intent.ACTION_SEND

                putExtra(Intent.EXTRA_TITLE, title)
                putExtra(
                    Intent.EXTRA_TEXT,
                    text
                )

                type = "text/plain"
            }, null)
        )
    }
}

@Composable
actual fun closeAppHandler(): () -> Unit {
    val context = LocalContext.current

    return {
        (context as? Activity)?.finish()
    }
}