package de.kitshn.api.tandoor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

class TandoorClientTest {

    @Test
    fun testConfigureTimeouts() {
        val credentials = TandoorCredentials("https://example.com")
        val client = TandoorClient(credentials)

        val initialShortClient = client.httpClient
        val initialLongClient = client.longHttpClient
        assertEquals(10000L, client.timeoutSettings.shortTimeout)
        assertEquals(60000L, client.timeoutSettings.longTimeout)

        val newSettings = TandoorTimeoutSettings(shortTimeout = 5000L, longTimeout = 120000L)
        client.configureTimeouts(newSettings)

        assertEquals(newSettings, client.timeoutSettings)
        assertNotSame(initialShortClient, client.httpClient)
        assertNotSame(initialLongClient, client.longHttpClient)
        assertEquals(5000L, client.timeoutSettings.shortTimeout)
        assertEquals(120000L, client.timeoutSettings.longTimeout)
    }
}
