package de.kitshn.api.tandoor.model

import kotlinx.serialization.Serializable

@Serializable
data class TandoorUserSpaceGroup(
    val id: Int,
    val name: String? = null,
)

@Serializable
data class TandoorUserSpace(
    val id: Int,
    val household: TandoorHousehold? = null,
    val groups: List<TandoorUserSpaceGroup> = emptyList(),
    val active: Boolean = false,
)
