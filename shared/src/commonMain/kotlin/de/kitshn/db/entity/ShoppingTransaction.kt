package de.kitshn.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class ShoppingListEntryOfflineActions {
    CHECK,
    UNCHECK,
    DELETE,
    UPDATE_AMOUNT,
    UPDATE_UNIT,
}

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ShoppingItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("entryId"), Index("entryId", "action")],
)
data class ShoppingTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val entryId: Int,
    val action: ShoppingListEntryOfflineActions,
    val timestamp: Long = 0L,
)
