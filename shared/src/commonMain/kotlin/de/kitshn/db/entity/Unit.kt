package de.kitshn.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.kitshn.api.tandoor.model.TandoorUnit

@Entity(tableName = "unit")
data class UnitEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val plural_name: String? = null,
    val description: String? = null,
    val base_unit: String? = null,
    val open_data_slug: String? = null
)

fun UnitEntity.toModel() = TandoorUnit(
    id = id,
    name = name,
    plural_name = plural_name,
    description = description,
    base_unit = base_unit,
    open_data_slug = open_data_slug
)

fun TandoorUnit.toEntity() = UnitEntity(
    id = id,
    name = name,
    plural_name = plural_name,
    description = description,
    base_unit = base_unit,
    open_data_slug = open_data_slug
)
