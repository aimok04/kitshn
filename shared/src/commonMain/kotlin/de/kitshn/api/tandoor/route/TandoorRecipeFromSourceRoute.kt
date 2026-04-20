package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeFromSource
import de.kitshn.api.tandoor.postObject
import de.kitshn.json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement

class TandoorRecipeFromSourceRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun fetch(url: String): TandoorRecipeFromSource {
        val data = buildJsonObject {
            put("url", JsonPrimitive(url))
            put("data", JsonPrimitive(""))
        }

        val recipeFromSource = json.decodeFromJsonElement<TandoorRecipeFromSource>(
            client.postObject(
                "/recipe-from-source/",
                data
            )
        )
        recipeFromSource.client = client
        return recipeFromSource
    }

}