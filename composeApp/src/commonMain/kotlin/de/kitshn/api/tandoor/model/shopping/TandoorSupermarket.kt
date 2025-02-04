package de.kitshn.api.tandoor.model.shopping

import kotlinx.serialization.Serializable

@Serializable
data class TandoorSupermarket(
    val id: Int,
    val name: String,
    val description: String? = null,
    val category_to_supermarket: List<TandoorSupermarketCategoryToSupermarket> = listOf()
)

@Serializable
data class TandoorSupermarketCategoryToSupermarket(
    val id: Int,
    val category: TandoorSupermarketCategory,
    val supermarket: Int,
    val order: Int = 0
)