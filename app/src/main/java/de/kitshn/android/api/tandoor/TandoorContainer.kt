package de.kitshn.android.api.tandoor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.kitshn.android.api.tandoor.model.TandoorFood
import de.kitshn.android.api.tandoor.model.TandoorKeyword
import de.kitshn.android.api.tandoor.model.TandoorKeywordOverview
import de.kitshn.android.api.tandoor.model.TandoorMealPlan
import de.kitshn.android.api.tandoor.model.TandoorMealType
import de.kitshn.android.api.tandoor.model.TandoorRecipeBook
import de.kitshn.android.api.tandoor.model.TandoorRecipeBookEntry
import de.kitshn.android.api.tandoor.model.log.TandoorCookLog
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.android.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.android.api.tandoor.route.TandoorSystemData

class TandoorContainer(
    val client: TandoorClient
) {

    var systemData by mutableStateOf<TandoorSystemData?>(null)

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

    val shoppingListEntries = mutableStateListOf<TandoorShoppingListEntry>()

}