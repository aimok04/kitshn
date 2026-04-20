package de.kitshn.api.tandoor.route

import com.eygraber.uri.Uri
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.TandoorPagedResponse
import de.kitshn.api.tandoor.model.TandoorSpace
import de.kitshn.api.tandoor.reqAny
import de.kitshn.json
import io.ktor.http.HttpMethod

class TandoorSpaceRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun list(
        page: Int = 1,
        pageSize: Int? = null
    ): TandoorPagedResponse<TandoorSpace> {
        val builder = Uri.Builder().appendEncodedPath("space/")
        if(pageSize != null) builder.appendQueryParameter("page_size", pageSize.toString())
        builder.appendQueryParameter("page", page.toString())

        val response = json.decodeFromString<TandoorPagedResponse<TandoorSpace>>(
            client.getObject(builder.build().toString()).toString()
        )

        return response
    }

    suspend fun listAll(): List<TandoorSpace> {
        var page = 1
        val entries = mutableListOf<TandoorSpace>()

        var response: TandoorPagedResponse<TandoorSpace>? = null
        while(response == null || response.next != null) {
            response = list(
                page = page,
                pageSize = 50
            )

            entries.addAll(response.results)
            page++
        }

        return entries
    }

    suspend fun current(): TandoorSpace {
        val response = json.decodeFromString<TandoorSpace>(
            client.getObject("/space/current/").toString()
        )

        return response
    }

    suspend fun switch(spaceId: Int): Boolean {
        // TODO: switch active space api
        client.reqAny("/switch-active-space/$spaceId/", HttpMethod.Get)
        return true
    }

}