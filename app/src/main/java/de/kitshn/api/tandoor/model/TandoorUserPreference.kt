package de.kitshn.api.tandoor.model

import de.kitshn.api.tandoor.route.TandoorUser
import kotlinx.serialization.Serializable

@Serializable
data class TandoorUserPreference(
    val user: Int,
    val default_unit: String? = null,
    val plan_share: List<TandoorUser>
)