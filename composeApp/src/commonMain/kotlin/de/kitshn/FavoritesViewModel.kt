package de.kitshn

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestState
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.api.tandoor.model.TandoorRecipeBook
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.internal_favorites_recipe_book_description
import kitshn.composeapp.generated.resources.internal_favorites_recipe_book_name
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

const val KITSHN_FAVORITE_RECIPE_BOOK_DESCRIPTION_TAG = "#KTSHNFAV"

class FavoritesViewModel : ViewModel() {

    var client: TandoorClient? = null

    fun init(client: TandoorClient) {
        this.client = client
        viewModelScope.launch {
            TandoorRequestState()
                .wrapRequest { getFavoritesRecipeBook() }
        }
    }

    private suspend fun createNewFavoritesRecipeBook(): TandoorRecipeBook {
        return client!!.recipeBook.create(
            name = getString(Res.string.internal_favorites_recipe_book_name),
            description = getString(Res.string.internal_favorites_recipe_book_description) + " — " + KITSHN_FAVORITE_RECIPE_BOOK_DESCRIPTION_TAG
        )
    }

    private var _favoritesBookId: Int? = null
    private suspend fun getFavoritesRecipeBook(): TandoorRecipeBook? {
        try {
            this.client?.container?.recipeBook?.get(_favoritesBookId)?.let {
                return it
            }

            val recipeBooks = client?.recipeBook?.list() ?: throw Error("COULD_NOT_FETCH_BOOKS")

            var favoritesBook = recipeBooks.firstOrNull {
                it.description.endsWith(KITSHN_FAVORITE_RECIPE_BOOK_DESCRIPTION_TAG)
            }
            if(favoritesBook == null) favoritesBook = createNewFavoritesRecipeBook()

            TandoorRequestState().wrapRequest { favoritesBook.listEntries() }
            _favoritesBookId = favoritesBook.id

            return favoritesBook
        } catch(e: TandoorRequestsError) {
            return null
        }
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
        favoritesBook?.createEntry(recipeId)
    }

    suspend fun removeFromFavorites(recipe: TandoorRecipeOverview) =
        removeFromFavorites(recipeId = recipe.id)

    suspend fun removeFromFavorites(recipeId: Int) {
        val favoritesBook = getFavoritesRecipeBook()
        favoritesBook?.entryByRecipeId?.get(recipeId)?.delete()
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
        return getFavoritesRecipeBook()?.id ?: -1
    }

}