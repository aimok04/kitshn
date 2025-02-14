package de.kitshn.api.tandoor.model.shopping

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorFood
import de.kitshn.api.tandoor.model.TandoorUnit
import de.kitshn.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
    val food: TandoorFood,
    val unit: TandoorUnit? = null,
    val amount: Double,
    val order: Long,
    @SerialName("checked")
    var _checked: Boolean,
    val created_by: TandoorShoppingListEntryCreatedBy,
    val recipe_mealplan: TandoorShoppingListEntryRecipeMealplan? = null,
    /*val created_at: String,
    val updated_at: String,*/
    /*val completed_at: Any?,
    val delay_until: Any?*/
) {
    @Transient
    var client: TandoorClient? = null

    var checked by mutableStateOf(_checked)

    var destroyed = false
    @Transient
    var _destroyed = destroyed

    suspend fun check() {
        client!!.shopping.check(setOf(id))
        checked = true
    }

    suspend fun uncheck() {
        client!!.shopping.uncheck(setOf(id))
        _checked = false
    }

    suspend fun delete() {
        client!!.shopping.delete(id)
        _destroyed = true
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

@Serializable
data class TandoorShoppingListEntryCreatedBy(
    val id: Int,
    val username: String,
    val display_name: String
)