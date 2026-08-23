package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.TandoorKeyword
import de.kitshn.api.tandoor.model.TandoorPagedResponse
import de.kitshn.api.tandoor.postObject
import de.kitshn.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

private const val PATH = "keyword/"

@Serializable
data class TandoorKeywordRouteListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<TandoorKeyword>
)

class TandoorKeywordRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun create(name: String, description: String): TandoorKeyword {
        val data = buildJsonObject {
            put("name", JsonPrimitive(name))
            put("description", JsonPrimitive(description))
        }

        val response = json.decodeFromString<TandoorKeyword>(
            client.postObject("/${PATH}", data).toString()
        )

        cache(response)
        return response
    }

    suspend fun list(
        query: String? = null,
        page: Int = 1,
        pageSize: Int?,
        updatedAt: String? = null
    ): TandoorPagedResponse<TandoorKeyword> {
        val response = listPage<TandoorKeyword>(
            path = PATH,
            page = page,
            pageSize = pageSize,
            query = query,
            extraParams = listOf("updated_at" to updatedAt)
        )

        // populate with client and store
        response.results.forEach { cache(it) }
        return response
    }

    suspend fun listAll(
        updatedAt: String? = null,
        onPageReceived: (suspend (List<TandoorKeyword>) -> Boolean)? = null,
        ): TandoorPagedResponse<TandoorKeyword> = listAllPages<TandoorKeyword>(
        path = "food/",
        pageSize = 200,
        extraParams = listOf("updated_at" to updatedAt),
    ) { page ->
        page.forEach { cache(it) }
        onPageReceived?.invoke(page) ?: false
    }


    suspend fun retrieve(
        id: Int
    ): TandoorKeyword {
        val response = json.decodeFromString<TandoorKeyword>(
            client.getObject("/keyword/${id}/").toString()
        )

        // populate with client and store
        cache(response)
        return response
    }

    private fun cache(keyword: TandoorKeyword) {
        client.container.keyword[keyword.id] = keyword
        client.container.keywordByName[keyword.name.lowercase()] = keyword
    }

}