package de.kitshn.api.tandoor.model

import kotlinx.serialization.Serializable

@Serializable
data class TandoorPagedResponse<T>(
    val count: Int,
    val next: String?,
    val previous: String?,
    val timestamp: String,
    val results: List<T>
)