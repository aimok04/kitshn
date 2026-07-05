package de.kitshn.api.tandoor

import co.touchlab.kermit.Logger
import java.io.ByteArrayInputStream
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509KeyManager
import javax.net.ssl.X509TrustManager
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Identity + extra trusted ca's the app uses then as well
 */
internal data class Pkcs12CertificateBundle(
    val keyManager: X509KeyManager,
    val extraCAs: List<X509Certificate>,
)

private const val TAG = "Pkcs12CertificateBundle"

/**
 * Decode a base64-encoded PKCS12 into a [Pkcs12CertificateBundle].
 * Returns null if either input is missing or the import fails (e.g. wrong
 * password, malformed data).
 */
@OptIn(ExperimentalEncodingApi::class)
internal fun loadPkcs12CertificateBundle(
    pkcs12DataBase64: String?,
    password: String?,
): Pkcs12CertificateBundle? {
    if (pkcs12DataBase64 == null || password == null) return null
    return try {
        val keyStore = KeyStore.getInstance("PKCS12").apply {
            load(ByteArrayInputStream(Base64.decode(pkcs12DataBase64)), password.toCharArray())
        }

        val keyManager = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            .apply { init(keyStore, password.toCharArray()) }
            .keyManagers
            .filterIsInstance<X509KeyManager>()
            .firstOrNull() ?: return null

        Pkcs12CertificateBundle(keyManager, extractCAs(keyStore))
    } catch (e: Exception) {
        Logger.e(e, tag = TAG) { "PKCS12 import failed" }
        null
    }
}

private fun extractCAs(keyStore: KeyStore): List<X509Certificate> =
    keyStore.aliases().toList().flatMap { alias ->
        // KeyEntries -> drop the client cert and keep the rest
        // CertificateEntries where getCertificateChain is null -> trust cert
        keyStore.getCertificateChain(alias)?.drop(1)
            ?: listOfNotNull(keyStore.getCertificate(alias))
    }.filterIsInstance<X509Certificate>()

/**
 * Create a trust manager that accepts default system + [extraCAs]
 */
internal fun trustManagerWithAnchors(extraCAs: List<X509Certificate>): X509TrustManager {
    if (extraCAs.isEmpty()) return systemTrustManager()

    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply { load(null, null) }
    systemTrustManager().acceptedIssuers.forEachIndexed { i, cert ->
        keyStore.setCertificateEntry("system-$i", cert)
    }
    extraCAs.forEachIndexed { i, cert ->
        keyStore.setCertificateEntry("p12-$i", cert)
    }

    return TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        .apply { init(keyStore) }
        .trustManagers
        .filterIsInstance<X509TrustManager>()
        .first()
}

private fun systemTrustManager(): X509TrustManager =
    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        .apply { init(null as KeyStore?) }
        .trustManagers
        .filterIsInstance<X509TrustManager>()
        .first()

/**
 * Build an SSLContext from both [keyManager] client credentials +
 * [trustManager] server validation
 *
 * Both managers must be installed on the context: OkHttp's
 * `sslSocketFactory(factory, tm)` does *not* override the trust manager
 * baked into the socket factory.
 */
internal fun buildSslContext(
    keyManager: X509KeyManager,
    trustManager: X509TrustManager,
): SSLContext = SSLContext.getInstance("TLS").apply {
    init(arrayOf(keyManager), arrayOf(trustManager), null)
}
