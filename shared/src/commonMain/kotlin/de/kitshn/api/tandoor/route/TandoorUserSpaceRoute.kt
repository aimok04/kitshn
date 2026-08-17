package de.kitshn.api.tandoor.route

import com.eygraber.uri.Uri
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getArray
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.TandoorUserSpace
import de.kitshn.api.tandoor.patchObject
import de.kitshn.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class TandoorUserSpaceRouteListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<TandoorUserSpace>
)

class TandoorUserSpaceRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun allPersonal(): List<TandoorUserSpace> {
        val response = client.getArray("/user-space/all_personal/")
        return json.decodeFromString<List<TandoorUserSpace>>(response.toString())
    }

    suspend fun list(
        page: Int = 1,
        pageSize: Int? = null
    ): TandoorUserSpaceRouteListResponse {
        val builder = Uri.Builder().appendEncodedPath("user-space/")
        if (pageSize != null) builder.appendQueryParameter("page_size", pageSize.toString())
        builder.appendQueryParameter("page", page.toString())

        return json.decodeFromString<TandoorUserSpaceRouteListResponse>(
            client.getObject(builder.build().toString()).toString()
        )
    }

    suspend fun retrieve(
        onPageReceived: (suspend (List<TandoorUserSpace>) -> Unit)? = null
    ): TandoorUserSpaceRouteListResponse {
        var page = 1
        var firstResponse: TandoorUserSpaceRouteListResponse? = null
        val allResults = mutableListOf<TandoorUserSpace>()
        while (true) {
            val response = list(page = page, pageSize = 200)
            if (firstResponse == null) firstResponse = response
            allResults.addAll(response.results)
            onPageReceived?.invoke(response.results)
            if (response.next == null) break
            page++
        }

        return firstResponse!!.copy(
            results = allResults,
            next = null,
            count = allResults.size
        )
    }

    suspend fun setHousehold(userSpace: TandoorUserSpace, householdId: Int): TandoorUserSpace {
        val body = buildJsonObject {
            put("household", buildJsonObject { put("id", JsonPrimitive(householdId)) })
            put("groups", JsonArray(userSpace.groups.map {
                buildJsonObject { put("id", JsonPrimitive(it.id)) }
            }))
        }
        return json.decodeFromString<TandoorUserSpace>(
            client.patchObject("/user-space/${userSpace.id}/", body).toString()
        )
    }
}
