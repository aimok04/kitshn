package de.kitshn.utils

import android.app.Activity
import android.security.KeyChain
import android.security.KeyChainAliasCallback
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import de.kitshn.AppActivity

actual class ClientCertificateSelector(private val activity: Activity) {
    actual fun selectClientCertificate(callback: (alias: String?) -> Unit) {
        KeyChain.choosePrivateKeyAlias(
            activity,
            object : KeyChainAliasCallback {
                override fun alias(alias: String?) {
                    callback(alias)
                }
            },
            null, null, null, -1, null
        )
    }
}

@Composable
actual fun rememberClientCertificateSelector(): ClientCertificateSelector {
    val context = LocalContext.current
    return remember(context) {
        // Assuming the context is an Activity or can be cast to one, which is true for Compose on Android usually.
        // It might be a wrapper (ContextWrapper), so we might need to unwrap, but casting usually works for direct Activity.
        // Keep it simple for now.
        ClientCertificateSelector(context as Activity)
    }
}
