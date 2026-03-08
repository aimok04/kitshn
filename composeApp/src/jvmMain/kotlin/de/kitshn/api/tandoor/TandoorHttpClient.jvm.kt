package de.kitshn.api.tandoor

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun createTandoorHttpClient(
    credentials: TandoorCredentials,
    onCertificateRequested: () -> Unit
): HttpClient {
    return HttpClient(OkHttp) {
        followRedirects = true
    }
}
