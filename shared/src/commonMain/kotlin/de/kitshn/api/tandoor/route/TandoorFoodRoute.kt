package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorFood
import de.kitshn.api.tandoor.model.TandoorPagedResponse
import de.kitshn.api.tandoor.postObject
import de.kitshn.json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class TandoorFoodRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun create(name: String): TandoorFood {
        val data = buildJsonObject { put("name", JsonPrimitive(name)) }
        return json.decodeFromString<TandoorFood>(
            client.postObject("/food/", data).toString()
        )
    }

    suspend fun list(
        query: String? = null,
        page: Int = 1,
        pageSize: Int? = null,
        updatedAt: String? = null,
    ): TandoorPagedResponse<TandoorFood> {
        val response = listPage<TandoorFood>(
            path = "food/",
            page = page,
            pageSize = pageSize,
            query = query,
            extraParams = listOf("updated_at" to updatedAt),
        )
        response.results.forEach { cache(it) }
        return response
    }

    suspend fun listAll(
        updatedAt: String? = null,
        onPageReceived: (suspend (List<TandoorFood>) -> Boolean)? = null,
    ): TandoorPagedResponse<TandoorFood> = listAllPages<TandoorFood>(
        path = "food/",
        pageSize = 200,
        extraParams = listOf("updated_at" to updatedAt),
    ) { page ->
        page.forEach { cache(it) }
        onPageReceived?.invoke(page) ?: false
    }

    private fun cache(food: TandoorFood) {
        client.container.food[food.id] = food
        client.container.foodByName[food.name.lowercase()] = food
    }
}
