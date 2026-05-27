package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.delete
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

private const val PATH = "shopping-list-entry/"
private const val LIST_PATH = "shopping-list/"

class TandoorShoppingRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun list(
        page: Int = 1,
        pageSize: Int? = null,
        query: String? = null,
    ): TandoorPagedResponse<TandoorShoppingListEntry> {
        val response = listPage<TandoorShoppingListEntry>(
            path = PATH,
            page = page,
            pageSize = pageSize,
            query = query,
        )

        response.results.forEach { cache(it) }

        return response
    }

    suspend fun listAll(
        updatedAfter: String? = null,
        onPageReceived: (suspend (List<TandoorShoppingListEntry>) -> Boolean)? = null,
    ): TandoorPagedResponse<TandoorShoppingListEntry> {
        return listAllPages<TandoorShoppingListEntry>(
            path = PATH,
            pageSize = 50,
            extraParams = listOf("updated_after" to updatedAfter)
        ) { page ->
            page.forEach { cache(it) }
            onPageReceived?.invoke(page) ?: false
        }
    }

    suspend fun listLists(
        page: Int = 1,
        pageSize: Int? = null,
        query: String? = null,
    ): TandoorPagedResponse<TandoorShoppingList> {
        return listPage<TandoorShoppingList>(
            path = LIST_PATH,
            page = page,
            pageSize = pageSize,
            query = query,
        )
    }

    suspend fun listAllLists(
        query: String? = null,
        onPageReceived: (suspend (List<TandoorShoppingList>) -> Boolean)? = null,
    ): TandoorPagedResponse<TandoorShoppingList> {
        return listAllPages<TandoorShoppingList>(
            path = LIST_PATH,
            pageSize = 50,
            query = query,
        ) { page ->
            onPageReceived?.invoke(page) ?: false
        }
    }

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
            client.postObject("/$PATH", data).toString()
        )

        cache(response)
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

        client.postObject("/${PATH}bulk/", data)
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

        client.postObject("/${PATH}bulk/", data)
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
        val entry = TandoorShoppingListEntry.parse(
            client,
            client.patchObject("/$PATH${entryId}/", data).toString()
        )
        cache(entry)
        return entry
    }

    suspend fun delete(
        id: Int
    ) {
        client.delete("/$PATH${id}/")
    }

    suspend fun delete(
        entries: List<TandoorShoppingListEntry>
    ) {
        entries.forEach {
            client.delete("/$PATH${it.id}/")
            it._destroyed = true
        }
    }

    private fun cache(entry: TandoorShoppingListEntry) {
        entry.client = client
        client.container.shoppingListEntries[entry.id] = entry
    }
}
