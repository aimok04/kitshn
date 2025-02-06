package de.kitshn.api.tandoor.model

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarketCategory
import de.kitshn.api.tandoor.patchObject
import de.kitshn.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

@Serializable
class TandoorFood(
    val id: Int,
    val name: String,
    val plural_name: String? = null,
    val description: String,
    val url: String? = null,
    val properties_food_amount: Double? = null,
    val properties_food_unit: TandoorUnit? = null,
    val fdc_id: Int? = null,
    val full_name: String,
    var supermarket_category: TandoorSupermarketCategory? = null,
    val ignore_shopping: Boolean = false,
    val open_data_slug: String? = null
) {
    suspend fun updateSupermarketCategory(
        client: TandoorClient,
        category: TandoorSupermarketCategory?
    ) {
        val data = buildJsonObject {
            put("supermarket_category", category?.let {
                buildJsonObject {
                    if(it.id != null) put("id", JsonPrimitive(it.id))
                    put("name", JsonPrimitive(it.name))
                    put("description", JsonPrimitive(it.description))
                }
            } ?: JsonNull)
        }

        val response = json.decodeFromString<TandoorFood>(
            client.patchObject("/food/${id}/", data).toString()
        )

        supermarket_category = response.supermarket_category
    }
}

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