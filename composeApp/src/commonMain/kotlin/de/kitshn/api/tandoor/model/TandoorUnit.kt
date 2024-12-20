package de.kitshn.api.tandoor.model

import kotlinx.serialization.Serializable

@Serializable
data class TandoorUnit(
    val id: Int,
    val name: String,
    val plural_name: String? = null,
    val description: String? = null,
    val base_unit: String? = null,
    val open_data_slug: String? = null
)