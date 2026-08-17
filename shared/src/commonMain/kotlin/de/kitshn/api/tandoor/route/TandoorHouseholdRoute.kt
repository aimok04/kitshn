package de.kitshn.api.tandoor.route

import com.eygraber.uri.Uri
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.delete
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.TandoorHousehold
import de.kitshn.api.tandoor.patchObject
import de.kitshn.api.tandoor.postObject
import de.kitshn.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

@Serializable
data class TandoorHouseholdRouteListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<TandoorHousehold>
)

class TandoorHouseholdRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun create(
        name: String
    ): TandoorHousehold {
        val data = buildJsonObject {
            put("name", JsonPrimitive(name))
        }

        return json.decodeFromString<TandoorHousehold>(
            client.postObject("/household/", data).toString()
        )
    }

    suspend fun list(
        page: Int = 1,
        pageSize: Int? = null
    ): TandoorHouseholdRouteListResponse {
        val builder = Uri.Builder().appendEncodedPath("household/")
        if(pageSize != null) builder.appendQueryParameter("page_size", pageSize.toString())
        builder.appendQueryParameter("page", page.toString())

        return json.decodeFromString<TandoorHouseholdRouteListResponse>(
            client.getObject(builder.build().toString()).toString()
        )
    }

    suspend fun retrieve(
        onPageReceived: (suspend (List<TandoorHousehold>) -> Unit)? = null
    ): TandoorHouseholdRouteListResponse {
        var page = 1
        var firstResponse: TandoorHouseholdRouteListResponse? = null
        val allResults = mutableListOf<TandoorHousehold>()
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

    suspend fun update(
        id: Int,
        name: String
    ): TandoorHousehold {
        val data = buildJsonObject {
            put("name", JsonPrimitive(name))
        }

        return json.decodeFromString<TandoorHousehold>(
            client.patchObject("/household/$id/", data).toString()
        )
    }

    suspend fun delete(id: Int) {
        client.delete("/household/$id/")
    }
}
