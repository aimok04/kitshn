package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.delete
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.PartialTandoorSpace
import de.kitshn.api.tandoor.model.TandoorPagedResponse
import de.kitshn.api.tandoor.model.TandoorSpace
import de.kitshn.api.tandoor.patchObject
import de.kitshn.api.tandoor.postObject
import de.kitshn.api.tandoor.reqAny
import de.kitshn.json
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

/** Used to encode [PartialTandoorSpace] without leaking nulls or defaults onto the wire. */
private val partialJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = false
}

private const val PATH = "space/"

class TandoorSpaceRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun list(
        page: Int = 1,
        pageSize: Int? = null,
    ): TandoorPagedResponse<TandoorSpace> {
        return listPage<TandoorSpace>(
            path = PATH,
            page = page,
            pageSize = pageSize,
        )
    }

    suspend fun retrieve(
        onPageReceived: (suspend (List<TandoorSpace>) -> Boolean)? = null,
    ): TandoorPagedResponse<TandoorSpace> {
        return listAllPages<TandoorSpace>(
            path = PATH,
            pageSize = 50,
        ) { page ->
            onPageReceived?.invoke(page) ?: false
        }
    }

    suspend fun current(): TandoorSpace = json.decodeFromString<TandoorSpace>(
        client.getObject("/space/current/").toString()
    )

    suspend fun switch(spaceId: Int): Boolean {
        client.reqAny("/switch-active-space/$spaceId/", HttpMethod.Get)
        return true
    }

    suspend fun create(partial: PartialTandoorSpace): TandoorSpace {
        val data = partialJson.encodeToJsonElement(partial).jsonObject
        return json.decodeFromString<TandoorSpace>(
            client.postObject("/space/", data).toString()
        )
    }

    suspend fun update(id: Int, partial: PartialTandoorSpace): TandoorSpace {
        val data = partialJson.encodeToJsonElement(partial).jsonObject
        return json.decodeFromString<TandoorSpace>(
            client.patchObject("/space/$id/", data).toString()
        )
    }

    suspend fun delete(id: Int) {
        client.delete("/space/$id/")
    }
}
