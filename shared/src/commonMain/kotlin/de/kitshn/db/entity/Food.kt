package de.kitshn.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import de.kitshn.api.tandoor.model.TandoorFood
import de.kitshn.api.tandoor.model.TandoorFoodRecipe
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntryFood

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["id"],
            childColumns = ["properties_food_unit_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = SupermarketCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["supermarket_category_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("name"),
        Index("properties_food_unit_id"),
        Index("supermarket_category_id"),
    ],
    tableName = "food",
)
data class FoodEntity(
    @PrimaryKey val id: Int,
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
        id = food.id,
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
        id = food.id,
        name = food.name,
        plural_name = food.plural_name,
        supermarket_category = supermarketCategory?.toModel(),
    )

fun TandoorFood.toEntity() =
    FoodEntity(
        id = id,
        name = name,
        plural_name = plural_name,
        description = description,
        url = url,
        recipe_id = recipe?.id,
        recipe_name = recipe?.name,
        recipe_url = recipe?.url,
        properties_food_amount = properties_food_amount,
        properties_food_unit_id = properties_food_unit?.id,
        fdc_id = fdc_id,
        full_name = full_name,
        supermarket_category_id = supermarket_category?.id,
        ignore_shopping = ignore_shopping,
        open_data_slug = open_data_slug,
    )

// Minimal projection from a shopping list entry's embedded food — used for hydrating
// FoodEntity rows when we only have the lite form. Combine with insertAllIfAbsent so
// a subsequent full FoodRepo.sync() isn't clobbered.
fun TandoorShoppingListEntryFood.toMinimalEntity() =
    FoodEntity(
        id = id,
        name = name,
        plural_name = plural_name,
        supermarket_category_id = supermarket_category?.id,
    )
