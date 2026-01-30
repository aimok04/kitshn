package de.kitshn.api.tandoor.model.shopping

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorFood
import de.kitshn.api.tandoor.model.TandoorMealPlan
import de.kitshn.api.tandoor.model.TandoorUnit
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.api.tandoor.patchObject
import de.kitshn.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

@Serializable
data class TandoorParsedIngredient(
    val amount: Double,
    val unit: String,
    val food: String,
    val note: String
)

@Serializable
class TandoorShoppingListEntry(
    val id: Int,
    val list_recipe: Long? = null,
    val food: TandoorShoppingListEntryFood,
    val unit: TandoorUnit? = null,
    val amount: Double,
    val order: Long,
    @SerialName("checked")
    var _checked: Boolean,
    val created_by: TandoorShoppingListEntryCreatedBy,
    val created_at: String? = null,
    val list_recipe_data: TandoorShoppingListEntryListRecipeData? = null
) {
    @Transient
    var client: TandoorClient? = null

    var checked by mutableStateOf(_checked)

    var destroyed = false
    @Transient
    var _destroyed = destroyed

    suspend fun partialUpdate(
        amount: Double? = null
    ): TandoorShoppingListEntry {
        val data = buildJsonObject {
            if(amount != null) put("amount", JsonPrimitive(amount))
        }

        return parse(
            client!!,
            client!!.patchObject("/shopping-list-entry/${id}/", data).toString()
        )
    }

    companion object {
        fun parse(client: TandoorClient, data: String): TandoorShoppingListEntry {
            val obj = json.decodeFromString<TandoorShoppingListEntry>(data)
            obj.client = client
            return obj
        }
    }
}

@Serializable
data class TandoorShoppingListEntryFood(
    val id: Int,
    val name: String,
    val plural_name: String? = null,
    var supermarket_category: TandoorSupermarketCategory? = null
) {
    suspend fun updateSupermarketCategory(
        client: TandoorClient,
        category: TandoorSupermarketCategory?
    ) {
        val data = buildJsonObject {
            put("supermarket_category", category?.let {
                buildJsonObject {
                    if(it.id != null) put("id", JsonPrimitive(it.id))
                    put("name", JsonPrimitive(it.name))
                    put("description", JsonPrimitive(it.description))
                }
            } ?: JsonNull)
        }

        val response = json.decodeFromString<TandoorFood>(
            client.patchObject("/food/${id}/", data).toString()
        )

        supermarket_category = response.supermarket_category
    }
}


@Serializable
data class TandoorShoppingListEntryListRecipeData(
    val id: Long,
    val name: String,
    val recipe: Int? = null,
    val recipe_data: TandoorRecipeOverview? = null,
    val mealplan: Int? = null,
    val meal_plan_data: TandoorMealPlan? = null,
    val servings: Double
)

@Serializable
data class TandoorShoppingListEntryCreatedBy(
    val id: Int,
    val username: String,
    val display_name: String
)