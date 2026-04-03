package de.kitshn.cache

import coil3.PlatformContext
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingList
import de.kitshn.json

class ShoppingListCache(
    context: PlatformContext,
    client: TandoorClient
) : BaseCache("SHOPPING_LIST", context, client) {

    fun update(lists: List<TandoorShoppingList>) {
        settings.putString("lists", json.encodeToString(lists))
    }

    fun retrieve() =
        settings.getStringOrNull("lists")?.let {
            json.decodeFromString<List<TandoorShoppingList>>(it)
        }

}