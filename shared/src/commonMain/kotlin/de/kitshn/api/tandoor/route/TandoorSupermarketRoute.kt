package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorPagedResponse
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarket
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarketCategory

class TandoorSupermarketRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun fetchAll(): List<TandoorSupermarket> =
        listAllPages<TandoorSupermarket>(
            path = "supermarket/",
            pageSize = 50,
            onPageReceived = { false },
        ).results

    suspend fun fetchAllCategories(
        updatedAt: String? = null,
        onPageReceived: (suspend (List<TandoorSupermarketCategory>) -> Boolean)? = null,
    ): TandoorPagedResponse<TandoorSupermarketCategory> = listAllPages<TandoorSupermarketCategory>(
        path = "supermarket-category/",
        pageSize = 50,
        extraParams = listOf("updated_at" to updatedAt),
        onPageReceived = { page -> onPageReceived?.invoke(page) ?: false },
    )
}
