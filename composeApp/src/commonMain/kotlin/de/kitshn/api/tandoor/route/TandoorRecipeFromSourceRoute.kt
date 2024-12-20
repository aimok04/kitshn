package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeImportResponse
import de.kitshn.api.tandoor.postObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class TandoorRecipeFromSourceRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun fetch(url: String): TandoorRecipeImportResponse {
        val data = buildJsonObject {
            put("url", JsonPrimitive(url))
            put("data", JsonPrimitive(""))
        }

        return TandoorRecipeImportResponse.parse(
            client,
            client.postObject("/recipe-from-source/", data).toString()
        )
    }

}