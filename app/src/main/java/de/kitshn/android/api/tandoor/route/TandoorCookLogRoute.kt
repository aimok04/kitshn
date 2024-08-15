package de.kitshn.android.api.tandoor.route

import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.TandoorRequestsError
import de.kitshn.android.api.tandoor.model.log.TandoorCookLog
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.android.api.tandoor.postObject
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TandoorCookLogRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    @Throws(TandoorRequestsError::class)
    suspend fun create(
        recipe: TandoorRecipe,
        servings: Int? = null,
        rating: Int? = null,
        comment: String
    ): TandoorCookLog {
        val date = LocalDateTime.now()
        val dateStr = date.format(DateTimeFormatter.ISO_DATE_TIME)

        val data = JSONObject().apply {
            put("recipe", recipe.id)
            put("servings", servings)
            put("rating", rating)
            put("comment", comment)
            put("created_at", dateStr)
        }

        val cookLog = TandoorCookLog.parse(
            this.client,
            client.postObject("/cook-log/", data).toString()
        )

        client.container.cookLog[cookLog.id] = cookLog
        return cookLog
    }

}