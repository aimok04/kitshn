package de.kitshn.api.tandoor

import android.security.KeyChain
import de.kitshn.AndroidApp
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManager
import javax.net.ssl.SSLContext
import javax.net.ssl.X509KeyManager
import java.net.Socket
import java.security.Principal

actual fun createTandoorHttpClient(
    credentials: TandoorCredentials,
    onCertificateRequested: () -> Unit
): HttpClient {
    return HttpClient(OkHttp) {
        followRedirects = true

        engine {
            config {
                sslSocketFactory(
                    SSLContext.getInstance("TLS").apply {
                        init(
                            arrayOf(
                                object : X509KeyManager {
                                    override fun getClientAliases(keyType: String?, issuers: Array<out Principal>?): Array<String>? = null
                                    
                                    override fun chooseClientAlias(keyType: Array<out String>?, issuers: Array<out Principal>?, socket: Socket?): String? {
                                        onCertificateRequested()
                                        return credentials.clientCertificateAlias
                                    }

                                    override fun getServerAliases(keyType: String?, issuers: Array<out Principal>?): Array<String>? = null
                                    
                                    override fun chooseServerAlias(keyType: String?, issuers: Array<out Principal>?, socket: Socket?): String? = null
                                    
                                    override fun getCertificateChain(alias: String?): Array<X509Certificate>? {
                                        return if (alias == credentials.clientCertificateAlias && alias != null) {
                                            KeyChain.getCertificateChain(AndroidApp.INSTANCE, alias)
                                        } else null
                                    }

                                    override fun getPrivateKey(alias: String?): PrivateKey? {
                                        return if (alias == credentials.clientCertificateAlias && alias != null) {
                                            KeyChain.getPrivateKey(AndroidApp.INSTANCE, alias)
                                        } else null
                                    }
                                }
                            ),
                            null,
                            null
                        )
                    }.socketFactory,
                    // We need a trust manager, but using the default system one is tricky to extract easily without custom setup.
                    // However, OkHttp uses the system default if we don't provide one, but we are setting the SSLSocketFactory.
                    // Let's try to get the default TrustManager.
                    getDefaultX509TrustManager()
                )
            }
        }
    }
}

private fun getDefaultX509TrustManager(): javax.net.ssl.X509TrustManager {
    val factory = javax.net.ssl.TrustManagerFactory.getInstance(javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm())
    factory.init(null as KeyStore?)
    return factory.trustManagers.first { it is javax.net.ssl.X509TrustManager } as javax.net.ssl.X509TrustManager
}
