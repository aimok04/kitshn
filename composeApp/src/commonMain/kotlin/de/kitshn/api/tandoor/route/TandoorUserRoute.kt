package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getArray
import de.kitshn.json
import kotlinx.serialization.Serializable

@Serializable
data class TandoorUser(
    val id: Int,
    val username: String = "",
    val first_name: String? = "",
    val last_name: String? = "",
    val display_name: String = ""
)

class TandoorUserRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun getUsers(): List<TandoorUser> {
        return json.decodeFromString<List<TandoorUser>>(
            client.getArray("/user/").toString()
        )
    }

    suspend fun getUserSpace(): TandoorUserSpace? {
        val resp = json.decodeFromString<TandoorUserSpaceResponse>(
            client.getObject("/user-space/").toString()
        )

        return resp.results.firstOrNull()
    }

    suspend fun get(): TandoorUser? {
        val userPreference = client.userPreference.fetch()
        val user = getUsers().find { it.id == userPreference.user.id }
        return user
    }

}