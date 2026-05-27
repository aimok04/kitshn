package de.kitshn.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingList

@Entity(
    tableName = "shopping_list",
    indices = [Index(value = ["id"], unique = true)],
)
data class ShoppingListEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val description: String,
    val color: String? = null,
)

fun ShoppingListEntity.toModel() = TandoorShoppingList(
    id = id,
    name = name,
    description = description,
    color = color,
)

fun TandoorShoppingList.toEntity() = ShoppingListEntity(
    id = id,
    name = name,
    description = description,
    color = color,
)
