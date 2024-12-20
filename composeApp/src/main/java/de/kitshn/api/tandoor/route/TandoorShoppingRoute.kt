package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.api.tandoor.getArray
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.api.tandoor.postObject
import de.kitshn.json
import org.json.JSONObject

class TandoorShoppingRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    @Throws(TandoorRequestsError::class)
    suspend fun add(amount: Double?, food: String?, unit: String?): TandoorShoppingListEntry {
        val data = JSONObject().apply {
            put("amount", amount ?: 0.0)
            put("food", food?.let { JSONObject().apply { put("name", food) } })
            put("unit", unit?.let { JSONObject().apply { put("name", unit) } })
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
        return response
    }

}