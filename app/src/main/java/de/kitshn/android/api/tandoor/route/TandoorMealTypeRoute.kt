package de.kitshn.android.api.tandoor.route

import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.TandoorRequestsError
import de.kitshn.android.api.tandoor.getArray
import de.kitshn.android.api.tandoor.model.TandoorMealType
import de.kitshn.android.json

class TandoorMealTypeRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    @Throws(TandoorRequestsError::class)
    suspend fun fetch(): List<TandoorMealType> {
        val response = json.decodeFromString<List<TandoorMealType>>(
            client.getArray("/meal-type/").toString()
        )

        response.forEach {
            client.container.mealType[it.id] = it
        }

        return response
    }

}