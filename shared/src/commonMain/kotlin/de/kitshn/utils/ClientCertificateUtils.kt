package de.kitshn.utils

import androidx.compose.runtime.Composable

data class ClientCertificateData(
    val alias: String? = null,
    val pkcs12DataBase64: String? = null,
    val pkcs12Password: String? = null,
)

expect class ClientCertificateSelector {
    fun selectCertificate(
        host: String? = null,
        port: Int? = null,
        callback: (ClientCertificateData?) -> Unit
    )
}

@Composable
expect fun rememberClientCertificateSelector(): ClientCertificateSelector
