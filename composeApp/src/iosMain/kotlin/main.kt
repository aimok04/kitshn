import androidx.compose.ui.window.ComposeUIViewController
import de.kitshn.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }