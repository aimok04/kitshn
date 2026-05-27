package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.delete
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.TandoorPagedResponse
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarket
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarketCategory
import de.kitshn.api.tandoor.postObject
import de.kitshn.json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class TandoorSupermarketRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun create(name: String): TandoorSupermarket {
        val data = buildJsonObject { put("name", JsonPrimitive(name)) }
        return json.decodeFromString<TandoorSupermarket>(
            client.postObject("/supermarket/", data).toString()
        )
    }

    suspend fun delete(remoteId: Int) {
        client.delete("/supermarket/${remoteId}/")
    }

    suspend fun retrieve(remoteId: Int): TandoorSupermarket =
        json.decodeFromString<TandoorSupermarket>(
            client.getObject("/supermarket/${remoteId}/").toString()
        )

    suspend fun list(
        query: String? = null,
        page: Int = 1,
        pageSize: Int? = null,
        updatedAt: String? = null,
    ): TandoorPagedResponse<TandoorSupermarket> = listPage<TandoorSupermarket>(
        path = "supermarket/",
        page = page,
        pageSize = pageSize,
        query = query,
        extraParams = listOf("updated_at" to updatedAt),
    )

    suspend fun listAll(
        updatedAt: String? = null,
        onPageReceived: (suspend (List<TandoorSupermarket>) -> Boolean)? = null,
    ): TandoorPagedResponse<TandoorSupermarket> = listAllPages<TandoorSupermarket>(
        path = "supermarket/",
        pageSize = 50,
        extraParams = listOf("updated_at" to updatedAt),
    ) { page ->
        onPageReceived?.invoke(page) ?: false
    }

    suspend fun createCategory(name: String): TandoorSupermarketCategory {
        val data = buildJsonObject { put("name", JsonPrimitive(name)) }
        return json.decodeFromString<TandoorSupermarketCategory>(
            client.postObject("/supermarket-category/", data).toString()
        )
    }

    suspend fun deleteCategory(remoteId: Int) {
        client.delete("/supermarket-category/${remoteId}/")
    }

    suspend fun retrieveCategory(remoteId: Int): TandoorSupermarketCategory =
        json.decodeFromString<TandoorSupermarketCategory>(
            client.getObject("/supermarket-category/${remoteId}/").toString()
        )

    suspend fun listCategories(
        query: String? = null,
        page: Int = 1,
        pageSize: Int? = null,
        updatedAt: String? = null,
    ): TandoorPagedResponse<TandoorSupermarketCategory> = listPage<TandoorSupermarketCategory>(
        path = "supermarket-category/",
        page = page,
        pageSize = pageSize,
        query = query,
        extraParams = listOf("updated_at" to updatedAt),
    )

    suspend fun listAllCategories(
        updatedAt: String? = null,
        onPageReceived: (suspend (List<TandoorSupermarketCategory>) -> Boolean)? = null,
    ): TandoorPagedResponse<TandoorSupermarketCategory> = listAllPages<TandoorSupermarketCategory>(
        path = "supermarket-category/",
        pageSize = 50,
        extraParams = listOf("updated_at" to updatedAt),
    ) { page ->
        onPageReceived?.invoke(page) ?: false
    }
}
