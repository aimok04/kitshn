package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.TandoorPagedResponse
import de.kitshn.json
import kotlinx.serialization.Serializable

@Serializable
data class TandoorAIProvider(
    val id: Int,
    val name: String,
    val description: String
)

class TandoorAIProviderRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun fetch(): List<TandoorAIProvider> {
        val response = json.decodeFromString<TandoorPagedResponse<TandoorAIProvider>>(
            client.getObject("/ai-provider/?page_size=100").toString()
        )

        response.results.forEach {
            client.container.aiProvider[it.id] = it
        }

        return response.results
    }

}