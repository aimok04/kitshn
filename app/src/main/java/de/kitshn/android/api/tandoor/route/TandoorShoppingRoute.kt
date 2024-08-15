package de.kitshn.android.api.tandoor.route

import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.TandoorRequestsError
import de.kitshn.android.api.tandoor.getArray
import de.kitshn.android.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.android.api.tandoor.postObject
import de.kitshn.android.json
import org.json.JSONObject

class TandoorShoppingRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    @Throws(TandoorRequestsError::class)
    suspend fun add(amount: Double?, food: String?, unit: String?): TandoorShoppingListEntry {
        val data = JSONObject().apply {
            put("amount", amount)
            put("food", JSONObject().apply { put("name", food) })
            put("unit", JSONObject().apply { put("name", unit) })
        }

        val response = TandoorShoppingListEntry.parse(
            client,
            client.postObject("/shopping-list-entry/", data).toString()
        )

        client.container.shoppingListEntries.add(0, response)
        return response
    }

    @Throws(TandoorRequestsError::class)
    suspend fun fetch(): List<TandoorShoppingListEntry> {
        val response = json.decodeFromString<List<TandoorShoppingListEntry>>(
            client.getArray("/shopping-list-entry/").toString()
        )

        response.forEach { it.client = client }

        client.container.shoppingListEntries.clear()
        client.container.shoppingListEntries.addAll(response)
        client.container.shoppingListEntries.reverse()

        return response
    }

}