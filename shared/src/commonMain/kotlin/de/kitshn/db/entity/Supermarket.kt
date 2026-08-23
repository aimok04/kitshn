package de.kitshn.db.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarket
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarketCategoryToSupermarket

@Entity(
    tableName = "supermarket",
    indices = [
        Index(value = ["remoteId"], unique = true)
    ]
)
data class SupermarketEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val localId: Int = 0,
    val remoteId: Int? = null,
    val name: String,
    val description: String? = null,
)

@Entity(
    tableName = "supermarket_category_to_supermarket",
    foreignKeys = [
        ForeignKey(
            entity = SupermarketEntity::class,
            parentColumns = ["id"],
            childColumns = ["supermarketLocalId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SupermarketCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryLocalId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("supermarketLocalId"), Index("categoryLocalId")],
)
data class SupermarketCategoryToSupermarketEntity(
    @PrimaryKey val id: Int,
    val supermarketLocalId: Int,
    val categoryLocalId: Int,
    val order: Int = 0,
)

data class SupermarketCategoryToSupermarketWithCategory(
    @Embedded val join: SupermarketCategoryToSupermarketEntity,
    @Relation(parentColumn = "categoryLocalId", entityColumn = "id")
    val category: SupermarketCategoryEntity,
)

data class SupermarketWithCategories(
    @Embedded val supermarket: SupermarketEntity,
    @Relation(
        entity = SupermarketCategoryToSupermarketEntity::class,
        parentColumn = "id",
        entityColumn = "supermarketLocalId",
    )
    val categories: List<SupermarketCategoryToSupermarketWithCategory>,
)

fun SupermarketWithCategories.toModel() = TandoorSupermarket(
    id = supermarket.localId,
    name = supermarket.name,
    description = supermarket.description,
    category_to_supermarket = categories.sortedBy { it.join.order }.map {
        TandoorSupermarketCategoryToSupermarket(
            id = it.join.id,
            category = it.category.toModel(),
            supermarket = supermarket.localId,
            order = it.join.order,
        )
    },
)

fun TandoorSupermarket.toEntity(localId: Int = 0) = SupermarketEntity(
    localId = localId,
    remoteId = id,
    name = name,
    description = description,
)
