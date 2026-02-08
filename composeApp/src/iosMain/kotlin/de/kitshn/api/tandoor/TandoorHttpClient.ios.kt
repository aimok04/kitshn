package de.kitshn.api.tandoor

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun createTandoorHttpClient(
    credentials: TandoorCredentials,
    onCertificateRequested: () -> Unit
): HttpClient {
    return HttpClient(Darwin) {
        followRedirects = true
        
        // TODO: Implement client certificate support for iOS
    }
}
