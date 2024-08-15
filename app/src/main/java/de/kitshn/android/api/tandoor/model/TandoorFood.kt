package de.kitshn.android.api.tandoor.model

import kotlinx.serialization.Serializable

@Serializable
data class TandoorFood(
    val id: Int,
    val name: String,
    val plural_name: String? = null,
    val description: String,
    val url: String? = null,
    val properties_food_amount: Double? = null,
    val properties_food_unit: TandoorUnit? = null,
    val fdc_id: Int? = null,
    val food_onhand: Boolean? = null,
    val full_name: String,
    val ignore_shopping: Boolean = false,
    val open_data_slug: String? = null
)