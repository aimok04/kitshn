package de.kitshn.android.api.tandoor.route

import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.TandoorRequestsError
import de.kitshn.android.api.tandoor.getObject
import de.kitshn.android.json
import kotlinx.serialization.Serializable

@Serializable
data class TandoorUserSpaceResponse(
    val count: Int,
    val results: List<TandoorUserSpace>
)

@Serializable
data class TandoorUserSpace(
    val user: TandoorUser
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

    @Throws(TandoorRequestsError::class)
    suspend fun getUsers(): List<TandoorUser> {
        return json.decodeFromString<List<TandoorUser>>(
            client.getObject("/user/").toString()
        )
    }

    @Throws(TandoorRequestsError::class)
    suspend fun get(): TandoorUser? {
        val resp = json.decodeFromString<TandoorUserSpaceResponse>(
            client.getObject("/user-space/").toString()
        )

        return resp.results.firstOrNull()?.user
    }

}