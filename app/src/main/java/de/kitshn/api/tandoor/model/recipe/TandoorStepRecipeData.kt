package de.kitshn.api.tandoor.model.recipe

import de.kitshn.api.tandoor.model.TandoorStep
import kotlinx.serialization.Serializable

@Serializable
data class TandoorStepRecipeData(
    val id: Int,
    val name: String,
    val steps: List<TandoorStep>
)