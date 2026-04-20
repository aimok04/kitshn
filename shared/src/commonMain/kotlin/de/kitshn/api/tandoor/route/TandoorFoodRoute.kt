package de.kitshn.api.tandoor.route

import com.eygraber.uri.Uri
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.TandoorFood
import de.kitshn.api.tandoor.postObject
import de.kitshn.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

@Serializable
data class TandoorFoodRouteListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<TandoorFood>
)

class TandoorFoodRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun create(
        name: String
    ): TandoorFood {
        val data = buildJsonObject {
            put("name", JsonPrimitive(name))
        }

        return json.decodeFromString<TandoorFood>(
            client.postObject("/food/", data).toString()
        )
    }

    suspend fun list(
        query: String? = null,
        page: Int = 1,
        pageSize: Int?
    ): TandoorFoodRouteListResponse {
        val builder = Uri.Builder().appendEncodedPath("food/")
        if(query != null) builder.appendQueryParameter("query", query)
        if(pageSize != null) builder.appendQueryParameter("page_size", pageSize.toString())
        builder.appendQueryParameter("page", page.toString())

        val response = json.decodeFromString<TandoorFoodRouteListResponse>(
            client.getObject(builder.build().toString()).toString()
        )

        // populate with client and store
        response.results.forEach {
            client.container.food[it.id] = it
            client.container.foodByName[it.name.lowercase()] = it
        }
        return response
    }

    suspend fun retrieve(): TandoorFoodRouteListResponse {
        return list(
            pageSize = 10000000
        )
    }

}