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
        pageSize: Int? = null
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

    suspend fun retrieve(
        onPageReceived: (suspend (List<TandoorFood>) -> Unit)? = null
    ): TandoorFoodRouteListResponse {
        var page = 1
        var firstResponse: TandoorFoodRouteListResponse? = null
        val allResults = mutableListOf<TandoorFood>()
        while (true) {
            val response = list(page = page, pageSize = 200)
            if (firstResponse == null) firstResponse = response
            allResults.addAll(response.results)
            onPageReceived?.invoke(response.results)
            if (response.next == null) break
            page++
        }

        return firstResponse.copy(
            results = allResults,
            next = null,
            count = allResults.size
        )
    }

}