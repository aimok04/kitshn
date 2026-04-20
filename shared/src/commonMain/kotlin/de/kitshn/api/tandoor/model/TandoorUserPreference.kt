package de.kitshn.api.tandoor.model

import de.kitshn.api.tandoor.route.TandoorUser
import kotlinx.serialization.Serializable

@Serializable
data class TandoorUserPreference(
    val user: TandoorUser,
    val default_unit: String? = null,
    val default_meal_type: TandoorMealType? = null
)