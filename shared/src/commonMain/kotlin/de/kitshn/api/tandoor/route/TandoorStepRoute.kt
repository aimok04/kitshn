package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getObject
import kotlinx.serialization.json.JsonObject

class TandoorStepRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun getRaw(
        id: Int
    ): JsonObject {
        return client.getObject("/step/${id}/")
    }

}