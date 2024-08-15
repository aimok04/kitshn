package de.kitshn.android.api.tandoor.route

import android.net.Uri
import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.TandoorRequestsError
import de.kitshn.android.api.tandoor.getObject
import de.kitshn.android.api.tandoor.model.TandoorKeyword
import de.kitshn.android.api.tandoor.postObject
import de.kitshn.android.json
import kotlinx.serialization.Serializable
import org.json.JSONObject

@Serializable
data class TandoorKeywordRouteListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<TandoorKeyword>
)

class TandoorKeywordRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    @Throws(TandoorRequestsError::class)
    suspend fun create(name: String, description: String): TandoorKeyword {
        val data = JSONObject().apply {
            put("name", name)
            put("description", description)
        }

        val response = json.decodeFromString<TandoorKeyword>(
            client.postObject("/keyword/", data).toString()
        )

        client.container.keyword[response.id] = response
        client.container.keywordByName[response.name.lowercase()] = response
        return response
    }

    @Throws(TandoorRequestsError::class)
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

    @Throws(TandoorRequestsError::class)
    suspend fun list(
        query: String? = null,
        page: Int = 1,
        pageSize: Int?
    ): TandoorKeywordRouteListResponse {
        val builder = Uri.Builder().appendPath("keyword")
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

    @Throws(TandoorRequestsError::class)
    suspend fun retrieve(): TandoorKeywordRouteListResponse {
        return list(
            pageSize = 10000000
        )
    }

}