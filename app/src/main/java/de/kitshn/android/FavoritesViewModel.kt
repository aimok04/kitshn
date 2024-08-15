package de.kitshn.android

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.TandoorRequestState
import de.kitshn.android.api.tandoor.model.TandoorRecipeBook
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipeOverview
import kotlinx.coroutines.launch

const val KITSHN_FAVORITE_RECIPE_BOOK_DESCRIPTION_TAG = "#KTSHNFAV"

@SuppressWarnings("StaticFieldLeak")
class FavoritesViewModel(
    app: Application,
    val context: Context
) : AndroidViewModel(app) {

    var client: TandoorClient? = null

    fun init(client: TandoorClient) {
        this.client = client
        viewModelScope.launch {
            val requestState = TandoorRequestState()
            requestState.wrapRequest { getFavoritesRecipeBook() }
        }
    }

    private suspend fun createNewFavoritesRecipeBook(): TandoorRecipeBook {
        return client!!.recipeBook.create(
            name = context.getString(R.string.internal_favorites_recipe_book_name),
            description = context.getString(R.string.internal_favorites_recipe_book_description) + " — " + KITSHN_FAVORITE_RECIPE_BOOK_DESCRIPTION_TAG
        )
    }

    private var _favoritesBookId: Int? = null
    private suspend fun getFavoritesRecipeBook(): TandoorRecipeBook {
        this.client?.container?.recipeBook?.get(_favoritesBookId)?.let {
            return it
        }

        val recipeBooks = client?.recipeBook?.list()
        var favoritesBook = recipeBooks?.firstOrNull {
            it.description.endsWith(KITSHN_FAVORITE_RECIPE_BOOK_DESCRIPTION_TAG)
        }
        if(favoritesBook == null) favoritesBook = createNewFavoritesRecipeBook()

        TandoorRequestState().wrapRequest { favoritesBook.listEntries() }
        _favoritesBookId = favoritesBook.id

        return favoritesBook
    }

    @Composable
    fun isFavorite(recipe: TandoorRecipeOverview) = isFavorite(recipeId = recipe.id)

    @Composable
    fun isFavorite(recipeId: Int): Boolean {
        return this.client?.container?.recipeBook?.get(_favoritesBookId)
            ?.entryByRecipeId?.containsKey(recipeId) ?: false
    }

    fun isFavorite(recipeId: Int, isComposable: Boolean = false): Boolean {
        return this.client?.container?.recipeBook?.get(_favoritesBookId)
            ?.entryByRecipeId?.containsKey(recipeId) ?: false
    }

    suspend fun addToFavorites(recipe: TandoorRecipeOverview) = addToFavorites(recipeId = recipe.id)
    suspend fun addToFavorites(recipeId: Int) {
        val favoritesBook = getFavoritesRecipeBook()
        favoritesBook.createEntry(recipeId)
    }

    suspend fun removeFromFavorites(recipe: TandoorRecipeOverview) =
        removeFromFavorites(recipeId = recipe.id)

    suspend fun removeFromFavorites(recipeId: Int) {
        val favoritesBook = getFavoritesRecipeBook()
        favoritesBook.entryByRecipeId[recipeId]?.delete()
    }

    suspend fun toggleFavorite(recipe: TandoorRecipeOverview) = toggleFavorite(recipeId = recipe.id)
    suspend fun toggleFavorite(recipeId: Int) {
        if(isFavorite(recipeId = recipeId, false)) {
            removeFromFavorites(recipeId = recipeId)
        } else {
            addToFavorites(recipeId = recipeId)
        }
    }

    fun getFavoritesRecipeBookIdSync(): Int {
        return _favoritesBookId ?: -1
    }

    suspend fun getFavoritesRecipeBookId(): Int {
        return getFavoritesRecipeBook().id
    }

}