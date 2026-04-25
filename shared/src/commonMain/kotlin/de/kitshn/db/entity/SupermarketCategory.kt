package de.kitshn.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarketCategory

@Entity(tableName = "supermarket_category")
data class SupermarketCategoryEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String? = null,
)

fun SupermarketCategoryEntity.toModel() =
    TandoorSupermarketCategory(
        id = id,
        name = name,
        description = description,
    )

// Server allows categories with null id (client-entered, not yet persisted); skip those.
fun TandoorSupermarketCategory.toEntity(): SupermarketCategoryEntity? {
    val id = this.id ?: return null
    return SupermarketCategoryEntity(id = id, name = name, description = description)
}
