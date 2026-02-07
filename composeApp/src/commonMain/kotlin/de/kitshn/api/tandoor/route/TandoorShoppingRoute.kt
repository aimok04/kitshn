package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.delete
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.TandoorPagedResponse
import de.kitshn.api.tandoor.model.shopping.TandoorParsedIngredient
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.api.tandoor.postObject
import de.kitshn.json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

class TandoorShoppingRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun add(amount: Double?, food: String?, unit: String?): TandoorShoppingListEntry {
        val ingredientParserData = buildJsonObject {
            put(
                "ingredient",
                JsonPrimitive(
                    "${amount?.let { "$it " } ?: ""}${unit?.let { "$it " } ?: ""}${food?.let { "$it " } ?: ""}"
                )
            )
        }

        val ingredientParserResponse =
            client.postObject("/ingredient-parser/post/", ingredientParserData)
        val ingredient = json.decodeFromJsonElement<TandoorParsedIngredient>(
            ingredientParserResponse["ingredient"] ?: throw NullPointerException()
        )

        val data = buildJsonObject {
            put("amount", JsonPrimitive(ingredient.amount))
            put("unit", json.encodeToJsonElement(ingredient.unit))
            put("food", json.encodeToJsonElement(ingredient.food))
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

    suspend fun fetchAll(): List<TandoorShoppingListEntry> {
        var page = 1
        val entries = mutableListOf<TandoorShoppingListEntry>()

        var response: TandoorPagedResponse<TandoorShoppingListEntry>? = null
        while(response == null || response.next != null) {
            response = json.decodeFromString<TandoorPagedResponse<TandoorShoppingListEntry>>(
                client.getObject("/shopping-list-entry/?page=$page&page_size=50").toString()
            )

            entries.addAll(response.results)
            page++
        }

        entries.forEach {
            it.client = client
            client.container.shoppingListEntries[it.id] = it
        }

        return entries
    }

}