package de.kitshn.api.tandoor.route

import com.eygraber.uri.Uri
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.TandoorUnit
import de.kitshn.api.tandoor.postObject
import de.kitshn.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

@Serializable
data class TandoorUnitRouteListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<TandoorUnit>
)

class TandoorUnitRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun create(
        name: String
    ): TandoorUnit {
        val data = buildJsonObject {
            put("name", JsonPrimitive(name))
        }

        return json.decodeFromString<TandoorUnit>(
            client.postObject("/unit/", data).toString()
        )
    }

    suspend fun list(
        query: String? = null,
        page: Int = 1,
        pageSize: Int?
    ): TandoorUnitRouteListResponse {
        val builder = Uri.Builder().appendEncodedPath("unit/")
        if(query != null) builder.appendQueryParameter("query", query)
        if(pageSize != null) builder.appendQueryParameter("page_size", pageSize.toString())
        builder.appendQueryParameter("page", page.toString())

        val response = json.decodeFromString<TandoorUnitRouteListResponse>(
            client.getObject(builder.build().toString()).toString()
        )

        // populate with client and store
        response.results.forEach {
            client.container.unit[it.id] = it
            client.container.unitByName[it.name.lowercase()] = it
        }
        return response
    }

    suspend fun retrieve(): TandoorUnitRouteListResponse {
        return list(
            pageSize = 10000000
        )
    }

}