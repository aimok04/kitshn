package de.kitshn.utils

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.action_abort
import kitshn.shared.generated.resources.action_apply
import kitshn.shared.generated.resources.settings_section_server_mtls_password_dialog_label
import kitshn.shared.generated.resources.settings_section_server_mtls_password_dialog_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

actual class ClientCertificateSelector {
    internal var showPasswordDialog by mutableStateOf(false)
    internal var passwordInput by mutableStateOf("")
    internal var pendingBytes: ByteArray? = null
    internal var pendingCallback: ((ClientCertificateData?) -> Unit)? = null
    internal var launcher: (() -> Unit)? = null

    actual fun selectCertificate(host: String?, port: Int?, callback: (ClientCertificateData?) -> Unit) {
        pendingCallback = callback
        launcher?.invoke()
    }
}

@OptIn(ExperimentalEncodingApi::class)
@Composable
actual fun rememberClientCertificateSelector(): ClientCertificateSelector {
    val coroutineScope = rememberCoroutineScope()
    val selector = remember { ClientCertificateSelector() }

    val filePickerLauncher = rememberFilePickerLauncher { file ->
        if (file != null) {
            coroutineScope.launch {
                val bytes = file.readBytes()
                selector.pendingBytes = bytes
                selector.showPasswordDialog = true
            }
        } else {
            selector.pendingCallback?.invoke(null)
            selector.pendingCallback = null
        }
    }

    selector.launcher = { filePickerLauncher.launch() }

    if (selector.showPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                selector.showPasswordDialog = false
                selector.passwordInput = ""
                selector.pendingCallback?.invoke(null)
                selector.pendingCallback = null
                selector.pendingBytes = null
            },
            title = { Text(stringResource(Res.string.settings_section_server_mtls_password_dialog_title)) },
            text = {
                TextField(
                    value = selector.passwordInput,
                    onValueChange = { selector.passwordInput = it },
                    label = { Text(stringResource(Res.string.settings_section_server_mtls_password_dialog_label)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                Button(onClick = {
                    val bytes = selector.pendingBytes ?: return@Button
                    selector.pendingCallback?.invoke(
                        ClientCertificateData(
                            pkcs12DataBase64 = Base64.encode(bytes),
                            pkcs12Password = selector.passwordInput,
                        )
                    )
                    selector.showPasswordDialog = false
                    selector.passwordInput = ""
                    selector.pendingCallback = null
                    selector.pendingBytes = null
                }) { Text(stringResource(Res.string.action_apply)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    selector.showPasswordDialog = false
                    selector.passwordInput = ""
                    selector.pendingCallback?.invoke(null)
                    selector.pendingCallback = null
                    selector.pendingBytes = null
                }) { Text(stringResource(Res.string.action_abort)) }
            }
        )
    }

    return selector
}
