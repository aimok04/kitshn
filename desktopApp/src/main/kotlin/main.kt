import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import de.kitshn.App
import de.kitshn.di.initKoin
import de.kitshn.disposeKcefBlocking
import de.kitshn.platformDetails

fun main(args: Array<String>) {
    if (System.getenv("XDG_SESSION_TYPE") == "wayland") {
        System.setProperty("skiko.linux.wayland.enabled", "true")
    }

    if (args.contains("--debug")) {
        System.setProperty("kitshn.debug", "true")
    }

    val isDebugMode = platformDetails.debug || System.getenv("KITSHN_DEBUG") == "true"

    val minSeverity = (System.getProperty("kitshn.log.level") ?: System.getenv("KITSHN_LOG_LEVEL"))?.let { level ->
        Severity.entries.find { it.name.equals(level, ignoreCase = true) }
    } ?: if (isDebugMode) {
        Severity.Debug
    } else {
        Severity.Info
    }
    Logger.setMinSeverity(minSeverity)

    initKoin()

    application {
        Window(
            title = "kitshn",
            state = rememberWindowState(width = 800.dp, height = 600.dp),
            onCloseRequest = ::exitApplication,
        ) {
            App()
        }

        DisposableEffect(Unit) {
            onDispose {
                disposeKcefBlocking()
            }
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}
