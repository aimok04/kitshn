package de.kitshn.api.tandoor.model

import kotlinx.serialization.Serializable

@Serializable
data class TandoorRecipeFilter(
    val id: Int,
    val name: String
)