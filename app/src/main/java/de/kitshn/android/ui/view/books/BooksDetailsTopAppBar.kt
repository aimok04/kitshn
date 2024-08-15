package de.kitshn.android.ui.view.books

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.TandoorRequestState
import de.kitshn.android.api.tandoor.model.TandoorRecipeBook
import de.kitshn.android.ui.component.buttons.BackButton
import de.kitshn.android.ui.component.icons.IconWithState
import de.kitshn.android.ui.dialog.recipeBook.RecipeBookEditDialogState
import de.kitshn.android.ui.selectionMode.SelectionModeState
import de.kitshn.android.ui.selectionMode.component.SelectionModeTopAppBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    SelectionModeTopAppBar(
        topAppBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(onBack)
                },
                title = {
                    Text(
                        text = if(isFavoriteBook) stringResource(id = R.string.common_favorites) else book.name,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                },
                actions = {
                    if(!isFavoriteBook) IconButton(
                        onClick = {
                            coroutineScope.launch {
                                editDialogState.open(book)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(id = R.string.action_edit)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        actions = {
            var showDeleteDialog by remember { mutableStateOf(false) }
            if(showDeleteDialog) AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = { Icon(Icons.Rounded.Delete, stringResource(id = R.string.action_delete)) },
                title = {
                    Text(
                        text = pluralStringResource(
                            id = R.plurals.action_delete_recipe_book_entries,
                            count = selectionModeState.selectedItems.size
                        )
                    )
                },
                text = {
                    Text(
                        text = pluralStringResource(
                            id = R.plurals.action_delete_recipe_book_entries_description,
                            count = selectionModeState.selectedItems.size
                        )
                    )
                },
                dismissButton = {
                    FilledTonalButton(
                        onClick = {
                            showDeleteDialog = false
                            selectionModeState.disable()
                        }
                    ) { Text(text = stringResource(id = R.string.action_abort)) }
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
                                }

                                selectionModeState.disable()

                                delay(100)
                                deleteRequestState.reset()
                            }
                        }
                    ) {
                        Text(text = stringResource(id = R.string.action_delete))
                    }
                }
            )

            IconButton(
                onClick = { showDeleteDialog = true }
            ) {
                IconWithState(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = stringResource(id = R.string.action_delete),
                    state = deleteRequestState.state.toIconWithState()
                )
            }
        },
        state = selectionModeState
    )
}