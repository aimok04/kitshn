package de.kitshn.android.api.tandoor.route

import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.TandoorRequestsError
import de.kitshn.android.api.tandoor.getArray
import de.kitshn.android.api.tandoor.model.TandoorUserPreference
import de.kitshn.android.json

class TandoorUserPreferenceRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    @Throws(TandoorRequestsError::class)
    suspend fun fetch(userId: Long? = null): TandoorUserPreference {
        val response = json.decodeFromString<TandoorUserPreference>(
            client.getArray("/user-preference/${if(userId != null) "$userId/" else ""}")?.get(0)
                .toString()
        )

        return response
    }

}