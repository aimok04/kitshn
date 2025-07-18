package de.kitshn.api.tandoor.route

import com.eygraber.uri.Uri
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.TandoorPagedResponse
import de.kitshn.api.tandoor.model.log.TandoorCookLog
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.api.tandoor.postObject
import de.kitshn.json
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

    suspend fun list(
        recipeId: Int? = null,
        page: Int = 1,
        pageSize: Int?
    ): TandoorPagedResponse<TandoorCookLog> {
        val builder = Uri.Builder().appendEncodedPath("cook-log/")
        if(recipeId != null) builder.appendQueryParameter("recipe", recipeId.toString())
        if(pageSize != null) builder.appendQueryParameter("page_size", pageSize.toString())
        builder.appendQueryParameter("page", page.toString())

        return json.decodeFromString<TandoorPagedResponse<TandoorCookLog>>(
            client.getObject(builder.build().toString()).toString()
        )
    }

}