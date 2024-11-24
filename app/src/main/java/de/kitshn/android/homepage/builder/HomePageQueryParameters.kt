package de.kitshn.android.homepage.builder

import de.kitshn.android.api.tandoor.route.TandoorRecipeQueryParametersSortOrder
import kotlinx.serialization.Serializable

@Serializable
data class HomePageQueryParameters(
    val query: String? = null,
    val new: Boolean? = null,
    val random: Boolean? = null,
    val keywords: List<String>? = null,
    val foods: List<String>? = null,
    val rating: Int? = null,
    val timescooked: Int? = null,
    var sortOrder: TandoorRecipeQueryParametersSortOrder? = null
)
