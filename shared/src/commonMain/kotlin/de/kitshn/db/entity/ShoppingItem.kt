package de.kitshn.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingList
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntryCreatedBy
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntryListRecipeData

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["id"],
            childColumns = ["unit_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = FoodEntity::class,
            parentColumns = ["id"],
            childColumns = ["food_id"],
            onDelete = ForeignKey.CASCADE
        )],
    indices = [
        Index("remoteId", unique = true),
        Index("unit_id"),
        Index("food_id")
    ]
)
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val remoteId: Int? = null,
    val list_recipe: Long? = null,
    val shopping_lists: List<TandoorShoppingList> = listOf(),
    val food_id: Int,
    val unit_id: Int? = null,
    val amount: Double,
    val order: Long,
    val checked: Boolean,
    val created_by: TandoorShoppingListEntryCreatedBy,
    val created_at: String? = null,
    val list_recipe_data: TandoorShoppingListEntryListRecipeData? = null,
    val meal_plan_id: Int? = null,
    val lastSyncError: String? = null,
)

data class ShoppingItemWithRelations(
    @Embedded val item: ShoppingItemEntity,
    @Relation(parentColumn = "unit_id", entityColumn = "id") val unit: UnitEntity?,
    @Relation(
        entity = FoodEntity::class, parentColumn = "food_id", entityColumn = "id"
    ) val food: FoodWithRelations?
)

fun ShoppingItemWithRelations.toModel(): TandoorShoppingListEntry? {
    val food = food ?: return null
    return TandoorShoppingListEntry(
        id = item.id,
        list_recipe = item.list_recipe,
        shopping_lists = item.shopping_lists,
        food = food.toShoppingListEntryFood(),
        unit = unit?.toModel(),
        amount = item.amount,
        order = item.order,
        _checked = item.checked,
        created_by = item.created_by,
        created_at = item.created_at,
        list_recipe_data = item.list_recipe_data
    )
}

// Caller must pre-translate the wire-format FK ids (food.id / unit?.id are remoteIds
// from the server) to *localIds* — typically via foodRepo.localIdByRemoteId(...) and
// unitRepo.localIdByRemoteId(...). The FK columns reference the local PKs.
//
// `localId` must be the existing row's PK when this entity is meant to update an
// already-present row (resolved via ShoppingDao.localIdByRemoteId / by reusing the
// offline-stub id). Default 0 lets Room auto-generate for fresh inserts.
fun TandoorShoppingListEntry.toEntity(
    foodLocalId: Int,
    unitLocalId: Int?,
    localId: Int = 0,
) = ShoppingItemEntity(
    id = localId,
    remoteId = id,
    list_recipe = list_recipe,
    shopping_lists = shopping_lists,
    food_id = foodLocalId,
    unit_id = unitLocalId,
    amount = amount,
    order = order,
    checked = checked,
    created_by = created_by,
    created_at = created_at,
    list_recipe_data = list_recipe_data,
)
