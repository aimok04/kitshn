package de.kitshn.utils

import android.security.KeyChain
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import de.kitshn.AndroidApp
import de.kitshn.api.tandoor.TandoorCredentials
import okhttp3.OkHttpClient
import java.security.KeyStore
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.net.Socket
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509KeyManager
import javax.net.ssl.X509TrustManager

actual fun createImageLoader(
    context: PlatformContext,
    credentials: TandoorCredentials
): ImageLoader {
    val okHttpClient = if (credentials.clientCertificateAlias != null) {
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(
                arrayOf(
                    object : X509KeyManager {
                        override fun getClientAliases(keyType: String?, issuers: Array<out Principal>?): Array<String>? = null

                        override fun chooseClientAlias(keyType: Array<out String>?, issuers: Array<out Principal>?, socket: Socket?): String? {
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
        }

        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore?)
        val trustManager = trustManagerFactory.trustManagers.first { it is X509TrustManager } as X509TrustManager

        OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .build()
    } else {
        OkHttpClient.Builder().build()
    }

    return ImageLoader.Builder(context)
        .components {
            add(OkHttpNetworkFetcherFactory(okHttpClient))
        }
        .build()
}
