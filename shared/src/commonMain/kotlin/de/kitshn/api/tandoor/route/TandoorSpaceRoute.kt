package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.TandoorPagedResponse
import de.kitshn.api.tandoor.model.TandoorSpace
import de.kitshn.api.tandoor.reqAny
import de.kitshn.json
import io.ktor.http.HttpMethod

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

    suspend fun listAll(
        onPageReceived: (suspend (List<TandoorSpace>) -> Boolean)? = null,
    ): TandoorPagedResponse<TandoorSpace> {
        return listAllPages<TandoorSpace>(
            path = PATH,
            pageSize = 50,
        ) { page ->
            onPageReceived?.invoke(page) ?: false
        }
    }

    suspend fun current(): TandoorSpace {
        val response = json.decodeFromString<TandoorSpace>(
            client.getObject("/${PATH}current/").toString()
        )

        return response
    }

    suspend fun switch(spaceId: Int): Boolean {
        // TODO: switch active space api
        client.reqAny("/switch-active-space/$spaceId/", HttpMethod.Get)
        return true
    }

}