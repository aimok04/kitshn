package de.kitshn.android.api.tandoor.route

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.TandoorRequestsError
import de.kitshn.android.api.tandoor.getObject
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.android.api.tandoor.postObject
import de.kitshn.android.json
import de.kitshn.android.toTFString
import kotlinx.serialization.Serializable
import org.json.JSONArray
import org.json.JSONObject

@Serializable
enum class TandoorRecipeQueryParametersSortOrder(
    val id: String,
    val label: Int,
    private val symbol: String
) {
    RELEVANCE("score", R.string.recipe_sorting_relevance, "1-9"),
    NEGATIVE_RELEVANCE("-score", R.string.recipe_sorting_relevance, "9-1"),
    NAME("name", R.string.common_name, "A-z"),
    NEGATIVE_NAME("-name", R.string.common_name, "Z-a"),
    LAST_COOKED("lastcooked", R.string.recipe_sorting_last_cooked, "↑"),
    NEGATIVE_LAST_COOKED("-lastcooked", R.string.recipe_sorting_last_cooked, "↓"),
    RATING("rating", R.string.common_review, "1-5"),
    NEGATIVE_RATING("-rating", R.string.common_review, "5-1"),
    TIMES_COOKED("favorite", R.string.recipe_sorting_cooking_frequency, "x-X"),
    NEGATIVE_TIMES_COOKED("-favorite", R.string.recipe_sorting_cooking_frequency, "X-x"),
    DATE_CREATED("created_at", R.string.recipe_sorting_creation_date, "↑"),
    NEGATIVE_DATE_CREATED("-created_at", R.string.recipe_sorting_creation_date, "↓"),
    LAST_VIEWED("lastviewed", R.string.recipe_sorting_last_viewed, "↑"),
    NEGATIVE_LAST_VIEWED("-lastviewed", R.string.recipe_sorting_last_viewed, "↓");

    @Composable
    fun itemLabel(): String {
        return "${stringResource(id = this.label)} (${this.symbol})"
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
    val rating: Int? = null,
    val timescooked: Int? = null,
    var sortOrder: TandoorRecipeQueryParametersSortOrder? = null
)

@Serializable
data class TandoorRecipeRouteListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<TandoorRecipeOverview>
)

class TandoorRecipeRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    @Throws(TandoorRequestsError::class)
    suspend fun create(data: JSONObject? = null): TandoorRecipe {
        val mData = data ?: JSONObject().apply {
            put("name", "New recipe")
            put("description", "This recipe is currently being created within the kitshn app.")
            put("steps", JSONArray())
        }

        val recipe = TandoorRecipe.parse(
            this.client,
            client.postObject("/recipe/", mData).toString()
        )

        client.container.recipe[recipe.id] = recipe
        return recipe
    }

    @Throws(TandoorRequestsError::class)
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
        return recipe
    }

    @Throws(TandoorRequestsError::class)
    suspend fun list(
        parameters: TandoorRecipeQueryParameters,
        page: Int = 1,
        pageSize: Int? = null
    ): TandoorRecipeRouteListResponse {
        val builder = Uri.Builder().appendPath("recipe")
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
        if(parameters.sortOrder != null) builder.appendQueryParameter(
            "sort_order",
            parameters.sortOrder?.id
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