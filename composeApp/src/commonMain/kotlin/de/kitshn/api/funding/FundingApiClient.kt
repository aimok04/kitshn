package de.kitshn.api.funding

import co.touchlab.kermit.Logger
import de.kitshn.json
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.http.isSuccess
import kotlin.coroutines.cancellation.CancellationException

class FundingApiClient(
    val url: String
) {

    private val httpClient = HttpClient {
        followRedirects = true
    }

    suspend fun state(): FundingStateResponse? {
        try {
            val response = httpClient.request {
                url("${this@FundingApiClient.url}/v1/state/")
            }

            if(!response.status.isSuccess())
                return null

            return json.decodeFromString(response.body<String>())
        } catch(_: CancellationException) {
            return null
        } catch(e: Exception) {
            Logger.e("FundingApiClient.kt", e)
            return null
        }
    }

}