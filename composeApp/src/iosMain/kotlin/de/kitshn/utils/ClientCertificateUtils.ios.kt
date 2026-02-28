package de.kitshn.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class ClientCertificateSelector {
    actual fun selectClientCertificate(callback: (alias: String?) -> Unit) {
        // TODO: Implement client certificate selection for iOS
        callback(null)
    }
}

@Composable
actual fun rememberClientCertificateSelector(): ClientCertificateSelector {
    return remember { ClientCertificateSelector() }
}
