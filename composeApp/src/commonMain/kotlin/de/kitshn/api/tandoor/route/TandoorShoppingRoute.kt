package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.delete
import de.kitshn.api.tandoor.getArray
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.api.tandoor.postObject
import de.kitshn.json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

class TandoorShoppingRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun add(amount: Double?, food: String?, unit: String?): TandoorShoppingListEntry {
        val data = buildJsonObject {
            put("amount", JsonPrimitive(amount ?: 0.0))
            put(
                "food",
                food?.let { buildJsonObject { put("name", JsonPrimitive(food)) } } ?: JsonNull
            )
            put(
                "unit",
                unit?.let { buildJsonObject { put("name", JsonPrimitive(unit)) } } ?: JsonNull
            )
        }

        val response = TandoorShoppingListEntry.parse(
            client,
            client.postObject("/shopping-list-entry/", data).toString()
        )

        client.container.shoppingListEntries[response.id] = response
        return response
    }

    suspend fun check(
        id: List<Int>
    ) {
        val data = buildJsonObject {
            put("ids", buildJsonArray {
                id.forEach { add(JsonPrimitive(it)) }
            })
            put("checked", JsonPrimitive(true))
        }

        client.postObject("/shopping-list-entry/bulk/", data)
    }

    suspend fun delete(
        id: Int
    ) {
        client.delete("/shopping-list-entry/${id}/")
    }

    suspend fun fetch(): List<TandoorShoppingListEntry> {
        val response = json.decodeFromString<List<TandoorShoppingListEntry>>(
            client.getArray("/shopping-list-entry/").toString()
        )

        response.forEach {
            it.client = client
            client.container.shoppingListEntries[it.id] = it
        }

        return response
    }

}