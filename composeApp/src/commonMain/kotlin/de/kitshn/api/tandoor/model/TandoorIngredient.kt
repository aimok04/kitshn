package de.kitshn.api.tandoor.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.kitshn.formatAmount
import kotlinx.serialization.Serializable

@Serializable
class TandoorIngredient(
    val id: Int,
    val food: TandoorFood? = null,
    val unit: TandoorUnit? = null,
    val amount: Double = 0.0,
    val note: String? = null,
    val order: Int,
    val is_header: Boolean,
    val no_amount: Boolean,
    val original_text: String? = null,
    val always_use_plural_unit: Boolean,
    val always_use_plural_food: Boolean
) {
    /**
     * used in IngredientsList.kt and IngredientItem.kt to track if ingredient has been used already
     */
    var tickedOff by mutableStateOf(false)

    fun formatAmount(amount: Double = this.amount, fractional: Boolean = true): String {
        return amount.formatAmount(fractional)
    }

    fun getLabel(amount: Double = this.amount): String {
        if((always_use_plural_food || amount > 1) && !food?.plural_name.isNullOrBlank()) return food!!.plural_name!!
        if(food?.name != null) return food.name
        return original_text ?: ""
    }

    fun getUnitLabel(amount: Double = this.amount): String {
        if((always_use_plural_unit || amount > 1) && !unit?.plural_name.isNullOrBlank()) return unit!!.plural_name!!
        if(unit?.name != null) return unit.name
        return ""
    }

    fun toString(
        scale: Double,
        fractional: Boolean = true
    ): String {
        val builder = StringBuilder()

        if(!no_amount && amount > 0.0) builder.append(formatAmount(amount * scale, fractional))
            .append(" ")

        if(!no_amount && unit != null) builder.append(getUnitLabel(amount * scale))
            .append(" ")

        builder.append(getLabel(amount * scale))

        return builder.toString()
    }
}