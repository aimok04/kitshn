package de.kitshn.android.api.tandoor.model.shopping

import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.model.TandoorFood
import de.kitshn.android.api.tandoor.model.TandoorUnit
import de.kitshn.android.api.tandoor.postObject
import de.kitshn.android.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.json.JSONArray
import org.json.JSONObject

@Serializable
data class TandoorParsedIngredient(
    val amount: Double,
    val unit: String,
    val food: String,
    val note: String
)

@Serializable
class TandoorShoppingListEntry(
    val id: Long,
    val list_recipe: Long? = null,
    val food: TandoorFood,
    val unit: TandoorUnit? = null,
    val amount: Double,
    val order: Long,
    val checked: Boolean,
    val recipe_mealplan: TandoorShoppingListEntryRecipeMealplan? = null,
    val created_at: String,
    val updated_at: String,
    /*val completed_at: Any?,
    val delay_until: Any?*/
) {
    @Transient
    var client: TandoorClient? = null

    suspend fun check() {
        val data = JSONObject().apply {
            put("ids", JSONArray().apply { put(id) })
            put("checked", true)
        }

        client!!.postObject("/shopping-list-entry/bulk/", data)
        client!!.container.shoppingListEntries.remove(this)
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
data class TandoorShoppingListEntryRecipeMealplan(
    val id: Long,
    val recipe_name: String,
    val name: String,
    val recipe: Int,
    val mealplan: Int? = null,
    val servings: Double,
    val mealplan_note: String? = null,
    val mealplan_from_date: String? = null,
    val mealplan_type: String? = null
)