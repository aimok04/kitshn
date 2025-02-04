package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getArray
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarket
import de.kitshn.json

class TandoorSupermarketRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun fetch(): List<TandoorSupermarket> {
        val response = json.decodeFromString<List<TandoorSupermarket>>(
            client.getArray("/supermarket/").toString()
        )

        client.container.supermarkets.clear()
        client.container.supermarkets.addAll(response)
        return response
    }

}