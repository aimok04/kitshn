package de.kitshn.ui.view.books

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import de.kitshn.api.tandoor.TandoorRequestState
import de.kitshn.api.tandoor.model.TandoorRecipeBook
import de.kitshn.ui.component.buttons.BackButton
import de.kitshn.ui.component.icons.IconWithState
import de.kitshn.ui.dialog.recipeBook.RecipeBookEditDialogState
import de.kitshn.ui.selectionMode.SelectionModeState
import de.kitshn.ui.selectionMode.component.SelectionModeTopAppBar
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_abort
import kitshn.composeapp.generated.resources.action_delete
import kitshn.composeapp.generated.resources.action_delete_recipe_book_entries
import kitshn.composeapp.generated.resources.action_delete_recipe_book_entries_description
import kitshn.composeapp.generated.resources.common_favorites
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewBooksDetailsTopAppBar(
    book: TandoorRecipeBook,
    isFavoriteBook: Boolean,

    scrollBehavior: TopAppBarScrollBehavior,

    selectionModeState: SelectionModeState<Int>,
    editDialogState: RecipeBookEditDialogState,
    deleteRequestState: TandoorRequestState,

    onBack: (() -> Unit)?
) {
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    SelectionModeTopAppBar(
        topAppBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    BackButton(onBack)
                },
                title = {
                    Text(
                        text = if(isFavoriteBook) stringResource(Res.string.common_favorites) else book.name,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                },
                scrollBehavior = scrollBehavior
            )
        },
        actions = {
            var showDeleteDialog by remember { mutableStateOf(false) }
            if(showDeleteDialog) AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = { Icon(Icons.Rounded.Delete, stringResource(Res.string.action_delete)) },
                title = {
                    Text(
                        text = pluralStringResource(
                            Res.plurals.action_delete_recipe_book_entries,
                            quantity = selectionModeState.selectedItems.size
                        )
                    )
                },
                text = {
                    Text(
                        text = pluralStringResource(
                            Res.plurals.action_delete_recipe_book_entries_description,
                            quantity = selectionModeState.selectedItems.size
                        )
                    )
                },
                dismissButton = {
                    FilledTonalButton(
                        onClick = {
                            showDeleteDialog = false
                            selectionModeState.disable()
                        }
                    ) { Text(text = stringResource(Res.string.action_abort)) }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false

                            coroutineScope.launch {
                                selectionModeState.selectedItems.forEach { recipeId ->
                                    deleteRequestState.wrapRequest {
                                        book.entryByRecipeId[recipeId]?.delete()
                                    }

                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    delay(25)
                                }

                                selectionModeState.disable()

                                delay(100)
                                deleteRequestState.reset()
                            }
                        }
                    ) {
                        Text(text = stringResource(Res.string.action_delete))
                    }
                }
            )

            IconButton(
                onClick = { showDeleteDialog = true }
            ) {
                IconWithState(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = stringResource(Res.string.action_delete),
                    state = deleteRequestState.state.toIconWithState()
                )
            }
        },
        state = selectionModeState
    )
}