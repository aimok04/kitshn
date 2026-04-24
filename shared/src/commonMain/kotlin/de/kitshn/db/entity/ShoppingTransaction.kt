package de.kitshn.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingList
import kotlinx.serialization.Serializable

@Serializable
enum class ShoppingListEntryOfflineActions {
    CHECK,
    UNCHECK,
    DELETE,
    CREATE,
    UPDATE,
}

@Entity
data class ShoppingTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val entryId: Int,
    val action: ShoppingListEntryOfflineActions,
    val payload: String? = null,
    val timestamp: Long = 0L
)

@Serializable
data class ShoppingCreatePayload(
    val foodName: String,
    val amount: Double,
    val unitName: String? = null,
    val shoppingLists: List<TandoorShoppingList> = listOf(),
    val mealPlanId: Int? = null,
    val listRecipeId: Long? = null,
    val order: Long? = null,
    val checked: Boolean = false,
)

@Serializable
data class ShoppingUpdatePayload(
    val amount: Double? = null,
    val unitName: String? = null,
    val clearUnit: Boolean = false,
)