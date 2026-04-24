package de.kitshn.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.kitshn.api.tandoor.model.TandoorUnit
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingList
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntryCreatedBy
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntryFood
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntryListRecipeData

@Entity
data class ShoppingItemEntity(
    @PrimaryKey val id: Int,
    val list_recipe: Long? = null,
    val shopping_lists: List<TandoorShoppingList> = listOf(),
    val food: TandoorShoppingListEntryFood,
    val unit: TandoorUnit? = null,
    val amount: Double,
    val order: Long,
    val checked: Boolean,
    val created_by: TandoorShoppingListEntryCreatedBy,
    val created_at: String? = null,
    val list_recipe_data: TandoorShoppingListEntryListRecipeData? = null
)

fun ShoppingItemEntity.toModel() = TandoorShoppingListEntry(
    id = id,
    list_recipe = list_recipe,
    shopping_lists = shopping_lists,
    food = food,
    unit = unit,
    amount = amount,
    order = order,
    _checked = checked,
    created_by = created_by,
    created_at = created_at,
    list_recipe_data = list_recipe_data
)

fun TandoorShoppingListEntry.toEntity() = ShoppingItemEntity(
    id = id,
    list_recipe = list_recipe,
    shopping_lists = shopping_lists,
    food = food,
    unit = unit,
    amount = amount,
    order = order,
    checked = checked,
    created_by = created_by,
    created_at = created_at,
    list_recipe_data = list_recipe_data
)