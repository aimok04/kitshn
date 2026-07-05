package de.kitshn.api.tandoor

import android.content.Context
import android.security.KeyChain
import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.mp.KoinPlatform
import java.net.Socket
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.X509Certificate
import javax.net.ssl.X509KeyManager

private const val TAG = "TandoorHttpClientFactory"

actual fun createTandoorHttpClient(
    credentials: TandoorCredentials,
    onCertificateRequested: () -> Unit,
): HttpClient {
    val pkcs12 = loadPkcs12CertificateBundle(
        credentials.mtlsCertificateData,
        credentials.mtlsCertificatePassword,
    )

    val keyManager = AndroidClientKeyManager(
        keyChainAlias = credentials.mtlsCertificateAlias,
        pkcs12KeyManager = pkcs12?.keyManager,
        keyChainContext = KoinPlatform.getKoin().get(),
        onCertificateRequested = onCertificateRequested,
    )
    val trustManager = trustManagerWithAnchors(pkcs12?.extraCAs.orEmpty())

    return HttpClient(OkHttp) {
        followRedirects = true
        engine {
            config {
                sslSocketFactory(
                    buildSslContext(keyManager, trustManager).socketFactory,
                    trustManager,
                )
            }
        }
    }
}

/**
 * Creates a [X509KeyManager] that notifies [onCertificateRequested] on
 * cert challenges.
 */
private class AndroidClientKeyManager(
    private val keyChainAlias: String?,
    private val pkcs12KeyManager: X509KeyManager?,
    private val keyChainContext: Context,
    private val onCertificateRequested: () -> Unit,
) : X509KeyManager {

    override fun chooseClientAlias(
        keyType: Array<out String>?,
        issuers: Array<out Principal>?,
        socket: Socket?,
    ): String? {
        onCertificateRequested()
        return keyChainAlias ?: pkcs12KeyManager?.chooseClientAlias(keyType, issuers, socket)
    }

    override fun getCertificateChain(alias: String?): Array<X509Certificate>? {
        if (alias == null) return null
        if (alias == keyChainAlias) return tryKeyChain {
            KeyChain.getCertificateChain(
                keyChainContext,
                alias
            )
        }
        return pkcs12KeyManager?.getCertificateChain(alias)
    }

    override fun getPrivateKey(alias: String?): PrivateKey? {
        if (alias == null) return null
        if (alias == keyChainAlias) return tryKeyChain {
            KeyChain.getPrivateKey(
                keyChainContext,
                alias
            )
        }
        return pkcs12KeyManager?.getPrivateKey(alias)
    }

    override fun getClientAliases(
        keyType: String?,
        issuers: Array<out Principal>?
    ): Array<String>? =
        pkcs12KeyManager?.getClientAliases(keyType, issuers)

    override fun getServerAliases(
        keyType: String?,
        issuers: Array<out Principal>?
    ): Array<String>? = null

    override fun chooseServerAlias(
        keyType: String?,
        issuers: Array<out Principal>?,
        socket: Socket?,
    ): String? = null

    private inline fun <T> tryKeyChain(block: () -> T): T? = try {
        block()
    } catch (e: Exception) {
        Logger.e(TAG, e) { "Android KeyChain access failed" }
        null
    }
}
