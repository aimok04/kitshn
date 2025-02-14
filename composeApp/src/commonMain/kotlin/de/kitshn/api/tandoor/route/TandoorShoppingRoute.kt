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
                food?.let { buildJsonObject { put("name", JsonPrimitive(food.trimEnd(' '))) } }
                    ?: JsonNull
            )
            put(
                "unit",
                unit?.let { buildJsonObject { put("name", JsonPrimitive(unit.trimEnd(' '))) } }
                    ?: JsonNull
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
        entries: List<TandoorShoppingListEntry>
    ) {
        check(entries.map { it.id }.toSet())
        entries.forEach { it.checked = true }
    }

    // using set to avoid jvm signature issue
    suspend fun check(
        id: Set<Int>
    ) {
        val data = buildJsonObject {
            put("ids", buildJsonArray {
                id.forEach { add(JsonPrimitive(it)) }
            })
            put("checked", JsonPrimitive(true))
        }

        client.postObject("/shopping-list-entry/bulk/", data)
    }

    suspend fun uncheck(
        entries: List<TandoorShoppingListEntry>
    ) {
        uncheck(entries.map { it.id }.toSet())
        entries.forEach { it.checked = false }
    }

    // using set to avoid jvm signature issue
    suspend fun uncheck(
        id: Set<Int>
    ) {
        val data = buildJsonObject {
            put("ids", buildJsonArray {
                id.forEach { add(JsonPrimitive(it)) }
            })
            put("checked", JsonPrimitive(false))
        }

        client.postObject("/shopping-list-entry/bulk/", data)
    }

    suspend fun delete(
        id: Int
    ) {
        client.delete("/shopping-list-entry/${id}/")
    }

    suspend fun delete(
        entries: List<TandoorShoppingListEntry>
    ) {
        entries.forEach {
            client.delete("/shopping-list-entry/${it.id}/")
            it._destroyed = true
        }
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