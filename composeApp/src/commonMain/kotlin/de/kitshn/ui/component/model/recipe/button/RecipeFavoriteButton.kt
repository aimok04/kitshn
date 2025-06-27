package de.kitshn.ui.component.model.recipe.button

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalHapticFeedback
import de.kitshn.FavoritesViewModel
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.handleTandoorRequestState
import de.kitshn.ui.TandoorRequestErrorHandler
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_add_to_favorites
import kitshn.composeapp.generated.resources.action_remove_from_favorites
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun RecipeFavoriteButton(
    recipeOverview: TandoorRecipeOverview,
    favoritesViewModel: FavoritesViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    val isFavorite = favoritesViewModel.isFavorite(recipeOverview)

    val toggleFavoriteRequestState = rememberTandoorRequestState()

    IconButton(onClick = {
        coroutineScope.launch {
            toggleFavoriteRequestState.wrapRequest {
                favoritesViewModel.toggleFavorite(recipeOverview)
            }

            hapticFeedback.handleTandoorRequestState(toggleFavoriteRequestState)
        }
    }) {
        when(isFavorite) {
            true -> Icon(
                Icons.Rounded.Favorite,
                stringResource(Res.string.action_remove_from_favorites)
            )

            else -> Icon(
                Icons.Rounded.FavoriteBorder,
                stringResource(Res.string.action_add_to_favorites)
            )
        }
    }

    TandoorRequestErrorHandler(state = toggleFavoriteRequestState)
}