package de.kitshn.ui.component.model.recipe.button

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import de.kitshn.FavoritesViewModel
import de.kitshn.R
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.ui.TandoorRequestErrorHandler
import kotlinx.coroutines.launch

@Composable
fun RecipeFavoriteButton(
    recipeOverview: TandoorRecipeOverview,
    favoritesViewModel: FavoritesViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val isFavorite = favoritesViewModel.isFavorite(recipeOverview)

    val toggleFavoriteRequestState = rememberTandoorRequestState()

    IconButton(onClick = {
        coroutineScope.launch {
            toggleFavoriteRequestState.wrapRequest {
                favoritesViewModel.toggleFavorite(recipeOverview)
            }
        }
    }) {
        when(isFavorite) {
            true -> Icon(
                Icons.Rounded.Favorite,
                stringResource(R.string.action_remove_from_favorites)
            )

            else -> Icon(
                Icons.Rounded.FavoriteBorder,
                stringResource(R.string.action_add_to_favorites)
            )
        }
    }

    TandoorRequestErrorHandler(state = toggleFavoriteRequestState)
}