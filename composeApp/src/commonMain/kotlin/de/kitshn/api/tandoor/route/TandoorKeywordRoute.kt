package de.kitshn.api.tandoor.route

import com.eygraber.uri.Uri
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.TandoorKeyword
import de.kitshn.api.tandoor.postObject
import de.kitshn.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

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
            client.postObject("/keyword/", data).toString()
        )

        client.container.keyword[response.id] = response
        client.container.keywordByName[response.name.lowercase()] = response
        return response
    }

    suspend fun retrieve(
        id: Int
    ): TandoorKeyword {
        val response = json.decodeFromString<TandoorKeyword>(
            client.getObject("/keyword/${id}/").toString()
        )

        // populate with client and store
        client.container.keyword[response.id] = response
        client.container.keywordByName[response.name] = response
        return response
    }

    suspend fun list(
        query: String? = null,
        page: Int = 1,
        pageSize: Int?
    ): TandoorKeywordRouteListResponse {
        val builder = Uri.Builder().appendEncodedPath("keyword/")
        if(query != null) builder.appendQueryParameter("query", query)
        if(pageSize != null) builder.appendQueryParameter("page_size", pageSize.toString())
        builder.appendQueryParameter("page", page.toString())

        val response = json.decodeFromString<TandoorKeywordRouteListResponse>(
            client.getObject(builder.build().toString()).toString()
        )

        // populate with client and store
        response.results.forEach {
            client.container.keyword[it.id] = it
            client.container.keywordByName[it.name.lowercase()] = it
        }
        return response
    }

    suspend fun retrieve(): TandoorKeywordRouteListResponse {
        return list(
            pageSize = 10000000
        )
    }

}