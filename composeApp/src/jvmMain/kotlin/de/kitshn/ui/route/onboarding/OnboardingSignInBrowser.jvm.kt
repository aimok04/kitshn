package de.kitshn.ui.route.onboarding

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.kitshn.ui.component.alert.FullSizeAlertPane
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

var IS_KCEF_INITIALIZED = false

// needed for jvm implementation
@Composable
actual fun InitializeWebView(
    onInitialized: () -> Unit
) {
    if(IS_KCEF_INITIALIZED) onInitialized()

    var progress by remember { mutableStateOf(0f) }

    var initialized by remember { mutableStateOf(false) }
    var restartRequired by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            KCEF.init(builder = {
                installDir(File("kcef-bundle"))

                progress {
                    onDownloading {
                        progress = it / 100f
                    }
                    onInitialized {
                        IS_KCEF_INITIALIZED = true

                        initialized = true
                        onInitialized()
                    }
                }
            }, onError = {
                it?.printStackTrace()
            }, onRestartRequired = {
                restartRequired = true
            })
        }
    }

    if(!initialized) {
        if(restartRequired) {
            FullSizeAlertPane(
                imageVector = Icons.Rounded.RestartAlt,
                contentDescription = "Please restart the app to continue.",
                text = "Please restart the app to continue."
            )
        } else {
            FullSizeAlertPane(
                imageVector = Icons.Rounded.Downloading,
                contentDescription = "Downloading additional library ...",
                text = "Downloading additional library ...",
                additionalContent = {
                    LinearProgressIndicator(
                        modifier = Modifier.padding(
                            top = 8.dp
                        ),
                        progress = progress
                    )

                    Text(
                        modifier = Modifier.padding(
                            top = 16.dp
                        ),
                        text = "CEF (Chromium Embedded Framework) is needed for this functionality.",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }
    }
}