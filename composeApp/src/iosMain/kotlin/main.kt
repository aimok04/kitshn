import androidx.compose.ui.window.ComposeUIViewController
import de.kitshn.App
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.allStringResources
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getString
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }

@OptIn(ExperimentalResourceApi::class)
fun lang(key: String): String {
    return runBlocking {
        Res.allStringResources[key]?.let { getString(it) } ?: "UNDEFINED"
    }
}