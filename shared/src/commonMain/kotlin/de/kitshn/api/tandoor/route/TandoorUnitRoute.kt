package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorPagedResponse
import de.kitshn.api.tandoor.model.TandoorUnit
import de.kitshn.api.tandoor.postObject
import de.kitshn.json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class TandoorUnitRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun create(name: String): TandoorUnit {
        val data = buildJsonObject { put("name", JsonPrimitive(name)) }
        return json.decodeFromString<TandoorUnit>(
            client.postObject("/unit/", data).toString()
        )
    }

    suspend fun list(
        query: String? = null,
        page: Int = 1,
        pageSize: Int? = null,
        updatedAt: String? = null,
    ): TandoorPagedResponse<TandoorUnit> {
        val response = listPage<TandoorUnit>(
            path = "unit/",
            page = page,
            pageSize = pageSize,
            query = query,
            extraParams = listOf("updated_at" to updatedAt),
        )
        response.results.forEach { cache(it) }
        return response
    }

    suspend fun retrieve(
        updatedAt: String? = null,
        onPageReceived: (suspend (List<TandoorUnit>) -> Boolean)? = null,
    ): TandoorPagedResponse<TandoorUnit> = listAllPages<TandoorUnit>(
        path = "unit/",
        pageSize = 200,
        extraParams = listOf("updated_at" to updatedAt),
    ) { page ->
        page.forEach { cache(it) }
        onPageReceived?.invoke(page) ?: false
    }

    private fun cache(unit: TandoorUnit) {
        client.container.unit[unit.id] = unit
        client.container.unitByName[unit.name.lowercase()] = unit
    }
}
