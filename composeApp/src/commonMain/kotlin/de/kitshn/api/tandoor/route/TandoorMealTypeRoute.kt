package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getArray
import de.kitshn.api.tandoor.model.TandoorMealType
import de.kitshn.json

class TandoorMealTypeRoute(client: TandoorClient) : TandoorBaseRoute(client) {

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