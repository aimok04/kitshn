package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.TandoorPagedResponse
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarket
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarketCategory
import de.kitshn.json

class TandoorSupermarketRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun fetchAll(): List<TandoorSupermarket> {
        var page = 1
        val entries = mutableListOf<TandoorSupermarket>()

        var response: TandoorPagedResponse<TandoorSupermarket>? = null
        while(response == null || response.next != null) {
            response = json.decodeFromString<TandoorPagedResponse<TandoorSupermarket>>(
                client.getObject("/supermarket/?page=$page&page_size=50").toString()
            )

            entries.addAll(response.results)
            page++
        }

        return entries
    }

    suspend fun fetchAllCategories(): List<TandoorSupermarketCategory> {
        var page = 1
        val entries = mutableListOf<TandoorSupermarketCategory>()

        var response: TandoorPagedResponse<TandoorSupermarketCategory>? = null
        while(response == null || response.next != null) {
            response = json.decodeFromString<TandoorPagedResponse<TandoorSupermarketCategory>>(
                client.getObject("/supermarket-category/?page=$page&page_size=50").toString()
            )

            entries.addAll(response.results)
            page++
        }

        return entries
    }

}