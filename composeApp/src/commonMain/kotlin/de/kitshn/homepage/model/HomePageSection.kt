package de.kitshn.homepage.model

import de.kitshn.api.tandoor.route.TandoorRecipeQueryParameters
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class HomePageSection(
    val title: String,
    val queryParameters: List<TandoorRecipeQueryParameters>
) {

    @Transient
    val loading: Boolean = false

    @Transient
    val recipeIds = mutableListOf<Int>()

}