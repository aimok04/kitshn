package de.kitshn.api.tandoor.model.shopping

import kotlinx.serialization.Serializable

@Serializable
data class TandoorSupermarketCategory(
    val id: Int,
    val name: String,
    val description: String? = null
)