package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class TandoorOpenApiData(
    val version: String
)

class TandoorOpenApiRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun get(): TandoorOpenApiData {
        val obj = client.getObject("@openapi/?format=openapi-json")
        val info = obj["info"]?.jsonObject

        val openapiData = TandoorOpenApiData(version = info?.get("version")?.jsonPrimitive?.content ?: "")
        client.container.openapiData = openapiData
        return openapiData
    }

}