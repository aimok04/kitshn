package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getArray
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarket
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarketCategory
import de.kitshn.json

class TandoorSupermarketRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun fetch(): List<TandoorSupermarket> {
        val response = json.decodeFromString<List<TandoorSupermarket>>(
            client.getArray("/supermarket/").toString()
        )

        return response
    }

    suspend fun fetchCategories(): List<TandoorSupermarketCategory> {
        val response = json.decodeFromString<List<TandoorSupermarketCategory>>(
            client.getArray("/supermarket-category/").toString()
        )

        return response
    }

}