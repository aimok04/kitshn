package de.kitshn.api.tandoor.model

import de.kitshn.api.tandoor.route.TandoorUser
import kotlinx.serialization.Serializable

@Serializable
data class TandoorUserSpaceGroup(
    val id: Int,
    val name: String? = null,
)

@Serializable
data class TandoorUserSpace(
    val id: Int,
    val user: TandoorUser? = null,
    val household: TandoorHousehold? = null,
    val groups: List<TandoorUserSpaceGroup> = emptyList(),
    val active: Boolean = false,
)
