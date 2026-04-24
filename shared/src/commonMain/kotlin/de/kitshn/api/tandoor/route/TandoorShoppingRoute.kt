package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.delete
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.TandoorPagedResponse
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingList
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.api.tandoor.patchObject
import de.kitshn.api.tandoor.postObject
import de.kitshn.json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement

class TandoorShoppingRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun add(
        amount: Double?,
        foodName: String? = null,
        foodId: Int? = null,
        unitName: String? = null,
        unitId: Int? = null,
        shoppingLists: List<TandoorShoppingList> = listOf(),
        mealPlanId: Int? = null,
        listRecipeId: Long? = null,
        order: Long? = null,
        checked: Boolean = false
    ): TandoorShoppingListEntry {
        val data = buildJsonObject {
            put("amount", JsonPrimitive(amount))
            put("checked", JsonPrimitive(checked))
            if (order != null) put("order", JsonPrimitive(order))
            if (mealPlanId != null) put("mealplan_id", JsonPrimitive(mealPlanId))
            if (listRecipeId != null) put("list_recipe", JsonPrimitive(listRecipeId))
            
            if (foodId != null || foodName != null) {
                put("food", buildJsonObject {
                    if (foodId != null && foodId > 0) put("id", JsonPrimitive(foodId))
                    if (foodName != null) put("name", JsonPrimitive(foodName))
                })
            }
            
            if (unitId != null || unitName != null) {
                put("unit", buildJsonObject {
                    if (unitId != null && unitId > 0) put("id", JsonPrimitive(unitId))
                    if (unitName != null) put("name", JsonPrimitive(unitName))
                })
            }

            put("shopping_lists", json.encodeToJsonElement(shoppingLists))
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

    suspend fun partialUpdate(
        entryId: Int,
        amount: Double? = null,
        unitId: Int? = null,
        unitName: String? = null,
        clearUnit: Boolean = false,
    ): TandoorShoppingListEntry {
        val data = buildJsonObject {
            if (amount != null) put("amount", JsonPrimitive(amount))
            when {
                clearUnit -> put("unit", kotlinx.serialization.json.JsonNull)
                unitId != null || unitName != null -> put("unit", buildJsonObject {
                    if (unitId != null && unitId > 0) put("id", JsonPrimitive(unitId))
                    if (unitName != null) put("name", JsonPrimitive(unitName))
                })
            }
        }
        return TandoorShoppingListEntry.parse(
            client,
            client.patchObject("/shopping-list-entry/${entryId}/", data).toString()
        )
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

    suspend fun fetchAllLists(): List<TandoorShoppingList> {
        var page = 1
        val entries = mutableListOf<TandoorShoppingList>()

        var response: TandoorPagedResponse<TandoorShoppingList>? = null
        while(response == null || response.next != null) {
            response = json.decodeFromString<TandoorPagedResponse<TandoorShoppingList>>(
                client.getObject("/shopping-list/?page=$page&page_size=50").toString()
            )

            entries.addAll(response.results)
            page++
        }

        return entries
    }

}
