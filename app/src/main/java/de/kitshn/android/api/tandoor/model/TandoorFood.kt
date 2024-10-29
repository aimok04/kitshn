package de.kitshn.android.api.tandoor.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

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
    val full_name: String,
    val ignore_shopping: Boolean = false,
    val open_data_slug: String? = null
)

@Serializable
data class TandoorFoodProperty(
    val id: Long,
    val name: String = "",
    val description: String? = "",
    val unit: String? = "",
    val order: Long = 0L,
    val food_values: JsonObject? = null,
    val total_value: Double = 0.0,
    val missing_value: Boolean = false
)