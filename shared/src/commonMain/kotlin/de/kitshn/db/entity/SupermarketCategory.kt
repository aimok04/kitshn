package de.kitshn.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarketCategory

// Two ids: see [UnitEntity] doc — same convention.
@Entity(
    tableName = "supermarket_category",
    indices = [
        Index(value = ["remoteId"], unique = true),
        Index(value = ["name"], unique = true),
    ],
)
data class SupermarketCategoryEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val localId: Int = 0,
    val remoteId: Int? = null,
    val name: String,
    val description: String? = null,
)

fun SupermarketCategoryEntity.toModel() = TandoorSupermarketCategory(
    id = localId,
    name = name,
    description = description,
)

fun TandoorSupermarketCategory.toEntity(localId: Int = 0) = SupermarketCategoryEntity(
    localId = localId,
    remoteId = id,
    name = name,
    description = description,
)

@Entity(tableName = "supermarket_category_pending_delete")
data class SupermarketCategoryPendingDeleteEntity(
    @PrimaryKey val remoteId: Int,
    val name: String,
)
