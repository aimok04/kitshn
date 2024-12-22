package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.log.TandoorCookLog
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.api.tandoor.postObject
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class TandoorCookLogRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun create(
        recipe: TandoorRecipe,
        servings: Int? = null,
        rating: Int? = null,
        comment: String
    ): TandoorCookLog {
        val date = LocalDateTime.Formats.ISO.format(
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )

        val data = buildJsonObject {
            put("recipe", JsonPrimitive(recipe.id))
            put("servings", JsonPrimitive(servings))
            put("rating", JsonPrimitive(rating))
            put("comment", JsonPrimitive(comment))
            put("created_at", JsonPrimitive(date))
        }

        val cookLog = TandoorCookLog.parse(
            this.client,
            client.postObject("/cook-log/", data).toString()
        )

        client.container.cookLog[cookLog.id] = cookLog
        return cookLog
    }

}