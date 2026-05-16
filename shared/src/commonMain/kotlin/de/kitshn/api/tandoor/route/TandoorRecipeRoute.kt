package de.kitshn.api.tandoor.route

import androidx.compose.runtime.Composable
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.TandoorPagedResponse
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.api.tandoor.postObject
import de.kitshn.toTFString
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.common_name
import kitshn.shared.generated.resources.common_review
import kitshn.shared.generated.resources.recipe_sorting_cooking_frequency
import kitshn.shared.generated.resources.recipe_sorting_creation_date
import kitshn.shared.generated.resources.recipe_sorting_last_cooked
import kitshn.shared.generated.resources.recipe_sorting_last_viewed
import kitshn.shared.generated.resources.recipe_sorting_relevance
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Serializable
enum class TandoorRecipeQueryParametersSortOrder(
    val id: String,
    val label: StringResource,
    private val symbol: String
) {
    RELEVANCE("score", Res.string.recipe_sorting_relevance, "1-9"),
    NEGATIVE_RELEVANCE("-score", Res.string.recipe_sorting_relevance, "9-1"),
    NAME("name", Res.string.common_name, "A-z"),
    NEGATIVE_NAME("-name", Res.string.common_name, "Z-a"),
    LAST_COOKED("lastcooked", Res.string.recipe_sorting_last_cooked, "↑"),
    NEGATIVE_LAST_COOKED("-lastcooked", Res.string.recipe_sorting_last_cooked, "↓"),
    RATING("rating", Res.string.common_review, "1-5"),
    NEGATIVE_RATING("-rating", Res.string.common_review, "5-1"),
    TIMES_COOKED("favorite", Res.string.recipe_sorting_cooking_frequency, "x-X"),
    NEGATIVE_TIMES_COOKED("-favorite", Res.string.recipe_sorting_cooking_frequency, "X-x"),
    DATE_CREATED("created_at", Res.string.recipe_sorting_creation_date, "↑"),
    NEGATIVE_DATE_CREATED("-created_at", Res.string.recipe_sorting_creation_date, "↓"),
    LAST_VIEWED("lastviewed", Res.string.recipe_sorting_last_viewed, "↑"),
    NEGATIVE_LAST_VIEWED("-lastviewed", Res.string.recipe_sorting_last_viewed, "↓");

    @Composable
    fun itemLabel(): String {
        return "${stringResource(this.label)} (${this.symbol})"
    }
}

@Serializable
data class TandoorRecipeQueryParameters(
    val query: String? = null,
    val new: Boolean? = null,
    val random: Boolean? = null,
    // Collection filters (map to repeated query params like keywords_and=1&keywords_and=2)
    val keywords: List<Int>? = null,
    val keywordsAnd: Boolean = false,
    val keywordsAndNot: List<Int>? = null,
    val keywordsOr: List<Int>? = null,
    val keywordsOrNot: List<Int>? = null,
    val foods: List<Int>? = null,
    val foodsAnd: Boolean = false,
    val foodsAndNot: List<Int>? = null,
    val foodsOr: List<Int>? = null,
    val foodsOrNot: List<Int>? = null,
    val books: List<Int>? = null,
    val booksAnd: Boolean = false,
    val booksAndNot: List<Int>? = null,
    val booksOr: List<Int>? = null,
    val booksOrNot: List<Int>? = null,
    val createdBy: TandoorUser? = null,
    val rating: Int? = null,
    val ratingGte: Int? = null,
    val ratingLte: Int? = null,
    val timescooked: Int? = null,
    val timesCookedGte: Int? = null,
    val timesCookedLte: Int? = null,
    var sortOrder: TandoorRecipeQueryParametersSortOrder? = null,
    val filter: Int? = null,
    val createdAt: String? = null,
    val createdAtGte: String? = null,
    val createdAtLte: String? = null,
    val updatedAt: String? = null,
    val updatedAtGte: String? = null,
    val updatedAtLte: String? = null,
    val cookedAtGte: String? = null,
    val cookedAtLte: String? = null,
    val viewedAtGte: String? = null,
    val viewedAtLte: String? = null,
    val includeChildren: Boolean? = null,
    val internal: Boolean? = null,
    val makeNow: Boolean? = null,
    val numRecent: Int? = null,
    val units: List<Int>? = null,
)

class TandoorRecipeRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun create(data: JsonObject? = null): TandoorRecipe {
        val mData = data ?: buildJsonObject {
            put("name", JsonPrimitive("New recipe"))
            put(
                "description",
                JsonPrimitive("This recipe is currently being created within the kitshn app.")
            )
            put("steps", buildJsonArray { })
            put("internal", JsonPrimitive(true))
        }

        val recipe = TandoorRecipe.parse(
            this.client,
            client.postObject("/recipe/", mData).toString()
        )

        client.container.recipe[recipe.id] = recipe
        return recipe
    }

    suspend fun retrieve(
        id: Int,
        cached: Boolean = false,
        share: String? = null
    ): TandoorRecipe {
        if(cached && client.container.recipe.contains(id)) return client.container.recipe[id]!!

        val recipe = TandoorRecipe.parse(
            this.client,
            client.getObject("/recipe/${id}/".let {
                if(share != null) {
                    "$it?share=$share"
                } else {
                    it
                }
            }).toString()
        )

        client.container.recipe[recipe.id] = recipe
        client.container.recipeOverview[recipe.id] = recipe.toOverview()
        return recipe
    }

    suspend fun list(
        parameters: TandoorRecipeQueryParameters,
        page: Int = 1,
        pageSize: Int? = null
    ): TandoorPagedResponse<TandoorRecipeOverview> {
        val response = listPage<TandoorRecipeOverview>(
            path = "recipe/",
            page = page,
            pageSize = pageSize,
            query = parameters.query,
            extraParams = buildExtraParams(parameters)
        )

        // populate with client and store
        response.results.forEach { cache(it) }

        return response
    }

    suspend fun listAll(
        parameters: TandoorRecipeQueryParameters,
        onPageReceived: (suspend (List<TandoorRecipeOverview>) -> Boolean)? = null,
    ): TandoorPagedResponse<TandoorRecipeOverview> {
        return listAllPages<TandoorRecipeOverview>(
            path = "recipe/",
            pageSize = 200,
            query = parameters.query,
            extraParams = buildExtraParams(parameters)
        ) { page ->
            page.forEach { cache(it) }
            onPageReceived?.invoke(page) ?: false
        }
    }

    private fun buildExtraParams(parameters: TandoorRecipeQueryParameters): List<Pair<String, String?>> = buildList {
        parameters.new?.let { add("new" to it.toTFString()) }
        parameters.random?.let { add("random" to it.toTFString()) }
        parameters.rating?.let { add("rating" to it.toString()) }
        parameters.ratingGte?.let { add("rating_gte" to it.toString()) }
        parameters.ratingLte?.let { add("rating_lte" to it.toString()) }
        parameters.timescooked?.let { add("timescooked" to it.toString()) }
        parameters.timesCookedGte?.let { add("timescooked_gte" to it.toString()) }
        parameters.timesCookedLte?.let { add("timescooked_lte" to it.toString()) }
        parameters.createdBy?.let { add("createdby" to it.id.toString()) }
        parameters.sortOrder?.let { add("sort_order" to it.id) }
        parameters.filter?.let { add("filter" to it.toString()) }
        parameters.createdAt?.let { add("createdon" to it) }
        parameters.createdAtGte?.let { add("createdon_gte" to it) }
        parameters.createdAtLte?.let { add("createdon_lte" to it) }
        parameters.updatedAt?.let { add("updatedon" to it) }
        parameters.updatedAtGte?.let { add("updatedon_gte" to it) }
        parameters.updatedAtLte?.let { add("updatedon_lte" to it) }
        parameters.cookedAtGte?.let { add("cookedon_gte" to it) }
        parameters.cookedAtLte?.let { add("cookedon_lte" to it) }
        parameters.viewedAtGte?.let { add("viewedon_gte" to it) }
        parameters.viewedAtLte?.let { add("viewedon_lte" to it) }
        parameters.includeChildren?.let { add("include_children" to it.toTFString()) }
        parameters.internal?.let { add("internal" to it.toTFString()) }
        parameters.makeNow?.let { add("makenow" to it.toTFString()) }
        parameters.numRecent?.let { add("num_recent" to it.toString()) }

        parameters.keywords?.forEach {
            add("keywords${if(parameters.keywordsAnd) "_and" else ""}" to it.toString())
        }
        parameters.keywordsAndNot?.forEach { add("keywords_and_not" to it.toString()) }
        parameters.keywordsOr?.forEach { add("keywords_or" to it.toString()) }
        parameters.keywordsOrNot?.forEach { add("keywords_or_not" to it.toString()) }

        parameters.foods?.forEach {
            add("foods${if(parameters.foodsAnd) "_and" else ""}" to it.toString())
        }
        parameters.foodsAndNot?.forEach { add("foods_and_not" to it.toString()) }
        parameters.foodsOr?.forEach { add("foods_or" to it.toString()) }
        parameters.foodsOrNot?.forEach { add("foods_or_not" to it.toString()) }

        parameters.books?.forEach {
            add("books${if(parameters.booksAnd) "_and" else ""}" to it.toString())
        }
        parameters.booksAndNot?.forEach { add("books_and_not" to it.toString()) }
        parameters.booksOr?.forEach { add("books_or" to it.toString()) }
        parameters.booksOrNot?.forEach { add("books_or_not" to it.toString()) }

        parameters.units?.forEach { add("units" to it.toString()) }
    }

    private fun cache(recipe: TandoorRecipeOverview) {
        recipe.client = client
        client.container.recipeOverview[recipe.id] = recipe
    }

}