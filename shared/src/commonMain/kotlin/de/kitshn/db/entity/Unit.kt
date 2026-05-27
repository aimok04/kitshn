package de.kitshn.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import de.kitshn.api.tandoor.model.TandoorUnit

// Two ids in play:
//   - localId: stable local PK (column kept as "id" so existing FKs / @Relation
//     references in food and ShoppingItemEntity keep working). This is what app code
//     uses everywhere — `toModel().id` returns the localId.
//   - remoteId: Tandoor server id, null while we haven't pushed yet. Lives in the entity
//     only; never exposed on the model. Only the sync layer uses it (push translates
//     localId → remoteId; ingest matches incoming remoteId to a local row).
@Entity(
    tableName = "unit",
    indices = [
        Index(value = ["remoteId"], unique = true),
        Index(value = ["name"], unique = true),
    ],
)
data class UnitEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val localId: Int = 0,
    val remoteId: Int? = null,
    val name: String,
    val plural_name: String? = null,
    val description: String? = null,
    val base_unit: String? = null,
    val open_data_slug: String? = null,
)

fun UnitEntity.toModel() = TandoorUnit(
    id = localId,
    name = name,
    plural_name = plural_name,
    description = description,
    base_unit = base_unit,
    open_data_slug = open_data_slug,
)

fun TandoorUnit.toEntity(localId: Int = 0) = UnitEntity(
    localId = localId,
    remoteId = id,
    name = name,
    plural_name = plural_name,
    description = description,
    base_unit = base_unit,
    open_data_slug = open_data_slug,
)

@Entity(tableName = "unit_pending_delete")
data class UnitPendingDeleteEntity(
    @PrimaryKey val remoteId: Int,
    val name: String,
)
