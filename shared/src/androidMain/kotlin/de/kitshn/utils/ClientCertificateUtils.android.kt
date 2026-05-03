package de.kitshn.utils

import android.app.Activity
import android.security.KeyChain
import android.security.KeyChainAliasCallback
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

actual class ClientCertificateSelector(private val activity: Activity) {
    actual fun selectCertificate(
        host: String?,
        port: Int?,
        callback: (ClientCertificateData?) -> Unit
    ) {
        KeyChain.choosePrivateKeyAlias(
            activity, { alias ->
                callback(
                    if (alias != null) ClientCertificateData(alias = alias) else null
                )
            }, null, null, host, port ?: -1, null
        )
    }
}

@Composable
actual fun rememberClientCertificateSelector(): ClientCertificateSelector {
    val context = LocalContext.current
    return remember(context) { ClientCertificateSelector(context as Activity) }
}
