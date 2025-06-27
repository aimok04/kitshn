package de.kitshn.ui.selectionMode.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import de.kitshn.KitshnViewModel
import de.kitshn.api.tandoor.delete
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.handleTandoorRequestState
import de.kitshn.ui.component.icons.IconWithState
import de.kitshn.ui.dialog.common.CommonDeletionDialog
import de.kitshn.ui.dialog.common.rememberCommonDeletionDialogState
import de.kitshn.ui.dialog.select.SelectRecipeBookDialog
import de.kitshn.ui.dialog.select.rememberSelectRecipeBookDialogState
import de.kitshn.ui.selectionMode.SelectionModeState
import de.kitshn.ui.selectionMode.component.SelectionModeTopAppBar
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_add_to_favorites
import kitshn.composeapp.generated.resources.action_add_to_recipe_books
import kitshn.composeapp.generated.resources.action_delete
import kitshn.composeapp.generated.resources.action_remove_from_favorites
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun RecipeSelectionModeTopAppBar(
    vm: KitshnViewModel,
    topAppBar: @Composable () -> Unit,
    state: SelectionModeState<Int>
) {
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    val addRecipesToBookRequestState = rememberTandoorRequestState()
    val addRecipesToFavoritesRequestState = rememberTandoorRequestState()
    val recipesDeleteRequestState = rememberTandoorRequestState()

    val isFavorite =
        state.selectedItems.firstOrNull { !vm.favorites.isFavorite(recipeId = it) } == null

    val addRecipesToBookSelectDialogState = rememberSelectRecipeBookDialogState()
    val recipesDeleteDialogState = rememberCommonDeletionDialogState<List<Int>>()

    SelectionModeTopAppBar(
        topAppBar = topAppBar,
        actions = {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        addRecipesToFavoritesRequestState.wrapRequest {
                            if(isFavorite) {
                                state.selectedItems.forEach { id ->
                                    if(!vm.favorites.isFavorite(
                                            recipeId = id,
                                            false
                                        )
                                    ) return@forEach
                                    vm.favorites.removeFromFavorites(id)

                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    delay(25)
                                }
                            } else {
                                state.selectedItems.forEach { id ->
                                    if(vm.favorites.isFavorite(recipeId = id, false)) return@forEach
                                    vm.favorites.addToFavorites(id)

                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    delay(25)
                                }
                            }
                        }
                    }
                }
            ) {
                IconWithState(
                    imageVector = when(isFavorite) {
                        true -> Icons.Rounded.Favorite
                        else -> Icons.Rounded.FavoriteBorder
                    },
                    contentDescription = when(isFavorite) {
                        true -> stringResource(Res.string.action_remove_from_favorites)
                        else -> stringResource(Res.string.action_add_to_favorites)
                    },
                    state = addRecipesToFavoritesRequestState.state.toIconWithState()
                )
            }

            IconButton(
                onClick = { addRecipesToBookSelectDialogState.open() }
            ) {
                IconWithState(
                    imageVector = Icons.Rounded.Book,
                    contentDescription = stringResource(Res.string.action_add_to_recipe_books),
                    state = addRecipesToBookRequestState.state.toIconWithState()
                )
            }

            IconButton(
                onClick = { recipesDeleteDialogState.open(state.selectedItems) }
            ) {
                IconWithState(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = stringResource(Res.string.action_delete),
                    state = recipesDeleteRequestState.state.toIconWithState()
                )
            }
        },
        state = state
    )

    if(vm.tandoorClient != null) {
        SelectRecipeBookDialog(
            client = vm.tandoorClient!!,
            favoritesRecipeBookId = vm.favorites.getFavoritesRecipeBookIdSync(),
            state = addRecipesToBookSelectDialogState
        ) { recipeBook ->
            coroutineScope.launch {
                addRecipesToBookRequestState.wrapRequest {
                    state.selectedItems.forEach { recipeId ->
                        if(recipeBook.entryByRecipeId[recipeId] != null) return@forEach
                        recipeBook.createEntry(recipeId)
                    }
                }

                hapticFeedback.handleTandoorRequestState(addRecipesToBookRequestState)
            }
        }

        CommonDeletionDialog(
            state = recipesDeleteDialogState,
            onConfirm = {
                coroutineScope.launch {
                    recipesDeleteRequestState.wrapRequest {
                        state.selectedItems.forEach { recipeId ->
                            vm.tandoorClient!!.delete("/recipe/${recipeId}/")

                            hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            delay(25)
                        }
                    }
                }
            }
        )
    }
}