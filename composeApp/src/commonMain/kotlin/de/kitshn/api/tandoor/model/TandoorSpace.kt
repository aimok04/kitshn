package de.kitshn.api.tandoor.model

import kotlinx.serialization.Serializable

@Serializable
data class TandoorSpace(
    val id: Int,
    val name: String,
    val created_at: String,
    val message: String
)