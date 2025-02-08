package de.kitshn.api.tandoor.model

import kotlinx.serialization.Serializable

@Serializable
data class TandoorScrapedSpace(
    val id: Int,
    val name: String,
    val active: Boolean
)