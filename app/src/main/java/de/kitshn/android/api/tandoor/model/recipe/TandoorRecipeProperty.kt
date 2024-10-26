package de.kitshn.android.api.tandoor.model.recipe

import kotlinx.serialization.Serializable

@Serializable
data class TandoorRecipeProperty(
    val id: Long,
    val property_amount: Double = 0.0,
    val property_type: TandoorRecipePropertyType,
)

@Serializable
data class TandoorRecipePropertyType(
    val id: Long,
    val name: String,
    val unit: String = "",
    val description: String = "",
    val order: Long = 0L
)