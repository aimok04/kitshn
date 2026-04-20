package de.kitshn.cache

import coil3.PlatformContext
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarket
import de.kitshn.json

class ShoppingSupermarketCache(
    context: PlatformContext,
    client: TandoorClient
) : BaseCache("SHOPPING_SUPERMARKET", context, client) {

    fun update(supermarkets: List<TandoorSupermarket>) {
        settings.putString("supermarkets", json.encodeToString(supermarkets))
    }

    fun retrieve() =
        settings.getStringOrNull("supermarkets")?.let {
            json.decodeFromString<List<TandoorSupermarket>>(it)
        }

}