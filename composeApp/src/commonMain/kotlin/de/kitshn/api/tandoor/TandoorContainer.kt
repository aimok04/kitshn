package de.kitshn.api.tandoor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.kitshn.api.tandoor.model.TandoorFood
import de.kitshn.api.tandoor.model.TandoorKeyword
import de.kitshn.api.tandoor.model.TandoorKeywordOverview
import de.kitshn.api.tandoor.model.TandoorMealPlan
import de.kitshn.api.tandoor.model.TandoorMealType
import de.kitshn.api.tandoor.model.TandoorRecipeBook
import de.kitshn.api.tandoor.model.TandoorRecipeBookEntry
import de.kitshn.api.tandoor.model.TandoorUnit
import de.kitshn.api.tandoor.model.log.TandoorCookLog
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarket
import de.kitshn.api.tandoor.route.TandoorOpenApiData

class TandoorContainer(
    val client: TandoorClient
) {

    var openapiData by mutableStateOf<TandoorOpenApiData?>(null)

    val cookLog = mutableStateMapOf<Int, TandoorCookLog?>()

    val keywordOverview = mutableStateMapOf<Int, TandoorKeywordOverview?>()
    val keyword = mutableStateMapOf<Int, TandoorKeyword?>()
    val keywordByName = mutableStateMapOf<String, TandoorKeyword?>()

    val recipeBook = mutableStateMapOf<Int, TandoorRecipeBook>()
    val recipeBookEntry = mutableStateMapOf<Int, TandoorRecipeBookEntry>()

    val food = mutableStateMapOf<Int, TandoorFood?>()
    val foodByName = mutableStateMapOf<String, TandoorFood?>()

    val recipeOverview = mutableStateMapOf<Int, TandoorRecipeOverview?>()
    val recipe = mutableStateMapOf<Int, TandoorRecipe?>()

    val mealPlan = mutableStateMapOf<Int, TandoorMealPlan?>()
    val mealType = mutableStateMapOf<Int, TandoorMealType?>()

    val unit = mutableStateMapOf<Int, TandoorUnit?>()
    val unitByName = mutableStateMapOf<String, TandoorUnit?>()

    val shoppingListEntries = mutableStateMapOf<Int, TandoorShoppingListEntry>()
    val supermarkets = mutableStateListOf<TandoorSupermarket>()

}