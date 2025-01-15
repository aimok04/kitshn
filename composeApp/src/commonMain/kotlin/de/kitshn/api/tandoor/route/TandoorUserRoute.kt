package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getObject
import de.kitshn.json
import kotlinx.serialization.Serializable

@Serializable
data class TandoorUserSpaceResponse(
    val count: Int,
    val results: List<TandoorUserSpace>
)

@Serializable
data class TandoorUserSpace(
    val user: TandoorUser,
    val space: Int = -1
)

@Serializable
data class TandoorUser(
    val id: Long,
    val username: String = "",
    val first_name: String? = "",
    val last_name: String? = "",
    val display_name: String = ""
)

class TandoorUserRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun getUsers(): List<TandoorUser> {
        return json.decodeFromString<List<TandoorUser>>(
            client.getObject("/user/").toString()
        )
    }

    suspend fun getUserSpace(): TandoorUserSpace? {
        val resp = json.decodeFromString<TandoorUserSpaceResponse>(
            client.getObject("/user-space/").toString()
        )

        return resp.results.firstOrNull()
    }

    suspend fun get(): TandoorUser? {
        val resp = json.decodeFromString<TandoorUserSpaceResponse>(
            client.getObject("/user-space/").toString()
        )

        return resp.results.firstOrNull()?.user
    }

}