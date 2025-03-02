package de.kitshn

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_copied_to_clipboard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import kotlin.system.exitProcess

@Composable
actual fun osDependentHapticFeedbackHandler(): ((type: HapticFeedbackType) -> Unit)? = null

@Composable
actual fun kotlinx.datetime.LocalDate.toHumanReadableDateLabelImpl(): String? = null

@Composable
actual fun BackHandler(enabled: Boolean, handler: () -> Unit) {
}

@Composable
actual fun KeepScreenOn() {
}

@Composable
actual fun launchMarketPageHandler(): () -> Unit {
    return {
        throw Exception("UTILS_JVM_launchMarketPageHandler_NOT_IMPLEMENTED")
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
        throw Exception("CURRENTLY_NOT_SUPPORTED_JVM")
    }
}

actual val isLaunchTimerHandlerImplemented = false

@Composable
actual fun shareContentHandler(): (title: String, text: String) -> Unit {
    val coroutineScope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var dialogText by remember { mutableStateOf("") }
    if(showDialog) AlertDialog(
        onDismissRequest = { showDialog = false },
        confirmButton = { },
        icon = {
            Icon(Icons.Rounded.ContentPaste, stringResource(Res.string.common_copied_to_clipboard))
        },
        title = {
            Text(text = stringResource(Res.string.common_copied_to_clipboard))
        },
        text = {
            Text(text = dialogText)
        }
    )

    return { title, text ->
        val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(text), null)

        dialogText = "$title\n\n$text"
        showDialog = true

        coroutineScope.launch {
            delay(1500)
            showDialog = false
        }
    }
}

@Composable
actual fun closeAppHandler(): () -> Unit {
    return {
        exitProcess(0)
    }
}