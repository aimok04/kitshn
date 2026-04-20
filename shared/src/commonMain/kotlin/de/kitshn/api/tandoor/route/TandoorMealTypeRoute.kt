package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.TandoorMealType
import de.kitshn.api.tandoor.model.TandoorPagedResponse
import de.kitshn.json

class TandoorMealTypeRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun fetch(): List<TandoorMealType> {
        val response = json.decodeFromString<TandoorPagedResponse<TandoorMealType>>(
            client.getObject("/meal-type/?page_size=100").toString()
        )

        response.results.forEach {
            client.container.mealType[it.id] = it
        }

        return response.results
    }

}