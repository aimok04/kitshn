package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.api.tandoor.getArray
import de.kitshn.api.tandoor.model.TandoorUserPreference
import de.kitshn.json

class TandoorUserPreferenceRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun fetch(userId: Long? = null): TandoorUserPreference {
        val response = json.decodeFromString<TandoorUserPreference>(
            client.getArray("/user-preference/${if(userId != null) "$userId/" else ""}")?.get(0)
                .toString()
        )

        return response
    }

}