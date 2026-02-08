package de.kitshn.utils

import androidx.compose.runtime.Composable

expect class ClientCertificateSelector {
    fun selectClientCertificate(callback: (alias: String?) -> Unit)
}

@Composable
expect fun rememberClientCertificateSelector(): ClientCertificateSelector
