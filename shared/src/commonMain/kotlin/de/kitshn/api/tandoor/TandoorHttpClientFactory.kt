package de.kitshn.api.tandoor

import io.ktor.client.HttpClient

expect fun createTandoorHttpClient(
    credentials: TandoorCredentials,
    onCertificateRequested: () -> Unit,
): HttpClient
