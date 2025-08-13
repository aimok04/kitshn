package de.kitshn.api.tandoor.route

import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.api.tandoor.postObject
import de.kitshn.json
import de.kitshn.toTFString
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_name
import kitshn.composeapp.generated.resources.common_review
import kitshn.composeapp.generated.resources.recipe_sorting_cooking_frequency
import kitshn.composeapp.generated.resources.recipe_sorting_creation_date
import kitshn.composeapp.generated.resources.recipe_sorting_last_cooked
import kitshn.composeapp.generated.resources.recipe_sorting_last_viewed
import kitshn.composeapp.generated.resources.recipe_sorting_relevance
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
    val keywords: List<Int>? = null,
    val keywordsAnd: Boolean = false,
    val foods: List<Int>? = null,
    val foodsAnd: Boolean = false,
    val createdBy: TandoorUser? = null,
    val rating: Int? = null,
    val timescooked: Int? = null,
    var sortOrder: TandoorRecipeQueryParametersSortOrder? = null,
    val filter: Int? = null
)

@Serializable
data class TandoorRecipeRouteListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<TandoorRecipeOverview>
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

    suspend fun get(
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
    ): TandoorRecipeRouteListResponse {
        val builder = Uri.Builder().appendEncodedPath("recipe/")
        if(parameters.query != null) builder.appendQueryParameter("query", parameters.query)
        if(parameters.new != null) builder.appendQueryParameter("new", parameters.new.toTFString())
        if(parameters.random != null) builder.appendQueryParameter(
            "random",
            parameters.random.toTFString()
        )
        if(parameters.rating != null) builder.appendQueryParameter(
            "rating",
            parameters.rating.toString()
        )
        if(parameters.timescooked != null) builder.appendQueryParameter(
            "timescooked",
            parameters.timescooked.toString()
        )
        if(parameters.keywords != null) parameters.keywords.forEach {
            builder.appendQueryParameter(
                "keywords${if(parameters.keywordsAnd) "_and" else ""}",
                it.toString()
            )
        }
        if(parameters.foods != null) parameters.foods.forEach {
            builder.appendQueryParameter(
                "foods${if(parameters.foodsAnd) "_and" else ""}",
                it.toString()
            )
        }
        if(parameters.createdBy != null) builder.appendQueryParameter(
            "createdby",
            parameters.createdBy.id.toString()
        )
        if(parameters.sortOrder != null) builder.appendQueryParameter(
            "sort_order",
            parameters.sortOrder?.id
        )
        if(parameters.filter != null) builder.appendQueryParameter(
            "filter",
            parameters.filter.toString()
        )
        if(pageSize != null) builder.appendQueryParameter("page_size", pageSize.toString())
        builder.appendQueryParameter("page", page.toString())

        val response = json.decodeFromString<TandoorRecipeRouteListResponse>(
            client.getObject(builder.build().toString()).toString()
        )

        // populate with client and store
        response.results.forEach {
            it.client = client
            client.container.recipeOverview[it.id] = it
        }

        return response
    }

}