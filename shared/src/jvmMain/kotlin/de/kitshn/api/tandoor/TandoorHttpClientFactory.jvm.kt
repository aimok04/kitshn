package de.kitshn.api.tandoor

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import java.net.Socket
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.X509Certificate
import javax.net.ssl.X509KeyManager

actual fun createTandoorHttpClient(
    credentials: TandoorCredentials,
    onCertificateRequested: () -> Unit,
): HttpClient {
    val pkcs12 = loadPkcs12CertificateBundle(
        credentials.mtlsCertificateData,
        credentials.mtlsCertificatePassword,
    )

    val keyManager = NotifyingClientKeyManager(pkcs12?.keyManager, onCertificateRequested)
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
 * Wraps [delegate] (or stands in for a missing one) and signals
 * [onCertificateRequested] on every client-cert challenge. Lets the UI
 * react to the server demanding a certificate even when no PKCS12 has
 * been configured yet.
 */
private class NotifyingClientKeyManager(
    private val delegate: X509KeyManager?,
    private val onCertificateRequested: () -> Unit,
) : X509KeyManager {

    override fun chooseClientAlias(
        keyType: Array<out String>?,
        issuers: Array<out Principal>?,
        socket: Socket?,
    ): String? {
        onCertificateRequested()
        return delegate?.chooseClientAlias(keyType, issuers, socket)
    }

    override fun getCertificateChain(alias: String?): Array<X509Certificate>? =
        delegate?.getCertificateChain(alias)

    override fun getPrivateKey(alias: String?): PrivateKey? =
        delegate?.getPrivateKey(alias)

    override fun getClientAliases(keyType: String?, issuers: Array<out Principal>?): Array<String>? =
        delegate?.getClientAliases(keyType, issuers)

    override fun getServerAliases(keyType: String?, issuers: Array<out Principal>?): Array<String>? = null

    override fun chooseServerAlias(
        keyType: String?,
        issuers: Array<out Principal>?,
        socket: Socket?,
    ): String? = null
}
