package de.kitshn.db.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import de.kitshn.api.tandoor.model.TandoorFood
import de.kitshn.api.tandoor.model.TandoorFoodRecipe
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntryFood

// Two ids: see [UnitEntity] doc — same convention. `properties_food_unit_id` and
// `supermarket_category_id` are FKs to the *localId* columns of unit / supermarket_category.
@Entity(
    foreignKeys = [
        // RESTRICT mirrors Tandoor's PROTECT on Food.properties_food_unit (cookbook/models.py:802).
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["id"],
            childColumns = ["properties_food_unit_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = SupermarketCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["supermarket_category_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["remoteId"], unique = true),
        Index("name"),
        Index("properties_food_unit_id"),
        Index("supermarket_category_id"),
    ],
    tableName = "food",
)
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val localId: Int = 0,
    val remoteId: Int? = null,
    val name: String,
    val plural_name: String? = null,
    val description: String? = null,
    val url: String? = null,
    val recipe_id: Int? = null,
    val recipe_name: String? = null,
    val recipe_url: String? = null,
    val properties_food_amount: Double? = null,
    val properties_food_unit_id: Int? = null,
    val fdc_id: Int? = null,
    val full_name: String? = null,
    val supermarket_category_id: Int? = null,
    val ignore_shopping: Boolean = false,
    val open_data_slug: String? = null,
)

data class FoodWithRelations(
    @Embedded val food: FoodEntity,
    @Relation(parentColumn = "properties_food_unit_id", entityColumn = "id")
    val propertiesFoodUnit: UnitEntity?,
    @Relation(parentColumn = "supermarket_category_id", entityColumn = "id")
    val supermarketCategory: SupermarketCategoryEntity?,
)

fun FoodWithRelations.toModel() =
    TandoorFood(
        id = food.localId,
        name = food.name,
        plural_name = food.plural_name,
        description = food.description,
        url = food.url,
        recipe =
            food.recipe_id?.let {
                TandoorFoodRecipe(id = it, name = food.recipe_name ?: "", url = food.recipe_url)
            },
        properties_food_amount = food.properties_food_amount,
        properties_food_unit = propertiesFoodUnit?.toModel(),
        fdc_id = food.fdc_id,
        full_name = food.full_name,
        supermarket_category = supermarketCategory?.toModel(),
        ignore_shopping = food.ignore_shopping,
        open_data_slug = food.open_data_slug,
    )

// Projection for the shopping list entry's "lite" food view.
fun FoodWithRelations.toShoppingListEntryFood() =
    TandoorShoppingListEntryFood(
        id = food.localId,
        name = food.name,
        plural_name = food.plural_name,
        supermarket_category = supermarketCategory?.toModel(),
    )

fun TandoorShoppingListEntryFood.toFood() = TandoorFood(
    id = id,
    name = name,
    plural_name = plural_name,
    supermarket_category = supermarket_category,
)

fun TandoorFood.toEntity(
    localId: Int = 0,
    unitLocalId: Int? = null,
    categoryLocalId: Int? = null,
) = FoodEntity(
    localId = localId,
    remoteId = id,
    name = name,
    plural_name = plural_name,
    description = description,
    url = url,
    recipe_id = recipe?.id,
    recipe_name = recipe?.name,
    recipe_url = recipe?.url,
    properties_food_amount = properties_food_amount,
    properties_food_unit_id = unitLocalId,
    fdc_id = fdc_id,
    full_name = full_name,
    supermarket_category_id = categoryLocalId,
    ignore_shopping = ignore_shopping,
    open_data_slug = open_data_slug,
)

@Entity(tableName = "food_pending_delete")
data class FoodPendingDeleteEntity(
    @PrimaryKey val remoteId: Int,
    val name: String,
)

fun FoodEntity.toMinimalModel() = TandoorFood(
    id = localId,
    name = name,
    plural_name = plural_name,
    description = description,
    url = url,
    recipe = recipe_id?.let { TandoorFoodRecipe(id = it, name = recipe_name ?: "", url = recipe_url) },
    properties_food_amount = properties_food_amount,
    properties_food_unit = null,
    fdc_id = fdc_id,
    full_name = full_name,
    supermarket_category = null,
    ignore_shopping = ignore_shopping,
    open_data_slug = open_data_slug,
)
