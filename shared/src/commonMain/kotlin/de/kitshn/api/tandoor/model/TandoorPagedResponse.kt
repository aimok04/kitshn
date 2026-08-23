package de.kitshn.api.tandoor.model

import kotlinx.serialization.Serializable

@Serializable
data class TandoorPagedResponse<T>(
    val count: Int,
    val next: String?,
    val previous: String?,
    // unfortunately recipe/ paged response does not give out timestamps
    val timestamp: String? = null,
    val results: List<T>
)