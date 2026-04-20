package de.kitshn.homepage.model

import de.kitshn.api.tandoor.TandoorClient
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

    suspend fun populate(
        client: TandoorClient
    ): Boolean {
        val recipeIdList = mutableListOf<Int>()
        queryParameters.forEach { qp ->
            val recipes =
                client.recipe.list(parameters = qp, pageSize = 20).results.filter { r ->
                    !recipeIdList.contains(r.id)
                }

            recipes.forEach { r -> recipeIdList.add(r.id) }
        }

        if(recipeIdList.size < 2) return false
        recipeIds.addAll(recipeIdList)
        return true
    }

}