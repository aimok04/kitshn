package de.kitshn.android.api.tandoor.model

import de.kitshn.android.formatAmount
import kotlinx.serialization.Serializable

@Serializable
class TandoorIngredient(
    val id: Int,
    val food: TandoorFood? = null,
    val unit: TandoorUnit? = null,
    val amount: Double,
    val note: String? = null,
    val order: Int,
    val is_header: Boolean,
    val no_amount: Boolean,
    val original_text: String? = null,
    val always_use_plural_unit: Boolean,
    val always_use_plural_food: Boolean
) {
    fun formatAmount(amount: Double = this.amount): String {
        return amount.formatAmount()
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
        scale: Double
    ): String {
        val builder = StringBuilder()

        if(!no_amount && amount > 0.0) builder.append(formatAmount(amount * scale))
            .append(" ")

        if(!no_amount && unit != null) builder.append(getUnitLabel(amount * scale))
            .append(" ")

        builder.append(getLabel(amount * scale))

        return builder.toString()
    }
}