package de.kitshn.homepage.model

import co.touchlab.kermit.Logger
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.route.TandoorRecipeQueryParameters
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.coroutines.cancellation.CancellationException

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
        // dedupes for free
        val recipeIdList = linkedSetOf<Int>()
        queryParameters.forEach { qp ->
            val results = try {
                client.recipe.list(parameters = qp, pageSize = 20).results
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.w(
                    "HomePageSection",
                    e
                ) { "home page population query failed for section $title" }
                return@forEach
            }
            results.mapTo(recipeIdList) { it.id }
        }

        if (recipeIdList.size < 2) return false
        recipeIds.addAll(recipeIdList)
        return true
    }

}