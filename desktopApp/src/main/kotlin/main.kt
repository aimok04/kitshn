import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import de.kitshn.App
import de.kitshn.disposeKcefBlocking

fun main() = application {
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

@Preview
@Composable
fun AppPreview() {
    App()
}
