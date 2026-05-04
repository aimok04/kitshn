package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getArray
import de.kitshn.api.tandoor.model.TandoorUserSpace
import de.kitshn.api.tandoor.patchObject
import de.kitshn.json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class TandoorUserSpaceRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun allPersonal(): List<TandoorUserSpace> {
        val response = client.getArray("/user-space/all_personal/")
        return json.decodeFromString<List<TandoorUserSpace>>(response.toString())
    }

    suspend fun setHousehold(userSpaceId: Int, householdId: Int): TandoorUserSpace {
        val body = buildJsonObject {
            put("household", buildJsonObject { put("id", JsonPrimitive(householdId)) })
        }
        return json.decodeFromString<TandoorUserSpace>(
            client.patchObject("/user-space/$userSpaceId/", body).toString()
        )
    }
}
