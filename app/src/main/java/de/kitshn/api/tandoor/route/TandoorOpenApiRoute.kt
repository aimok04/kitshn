package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.api.tandoor.getObject

data class TandoorOpenApiData(
    val version: String
)

class TandoorOpenApiRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    @Throws(TandoorRequestsError::class)
    suspend fun get(): TandoorOpenApiData {
        val obj = client.getObject("@openapi/?format=openapi-json")
        val info = obj?.getJSONObject("info")

        val openapiData = TandoorOpenApiData(version = info?.getString("version") ?: "")
        client.container.openapiData = openapiData
        return openapiData
    }

}