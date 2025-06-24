package de.kitshn.ui.route.main.subroute.books

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestState
import de.kitshn.ui.component.icons.IconWithState
import de.kitshn.ui.dialog.recipeBook.RecipeBookEditDialogState
import de.kitshn.ui.selectionMode.SelectionModeState
import de.kitshn.ui.selectionMode.component.SelectionModeTopAppBar
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_abort
import kitshn.composeapp.generated.resources.action_delete
import kitshn.composeapp.generated.resources.action_delete_recipe_books
import kitshn.composeapp.generated.resources.action_delete_recipe_books_description
import kitshn.composeapp.generated.resources.action_edit
import kitshn.composeapp.generated.resources.navigation_books
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMainSubrouteBooksTopAppBar(
    client: TandoorClient,

    favoritesRecipeBookId: Int,

    colors: TopAppBarColors,
    scrollBehavior: TopAppBarScrollBehavior,

    selectionModeState: SelectionModeState<Int>,
    editDialogState: RecipeBookEditDialogState,
    deleteRequestState: TandoorRequestState,
    onUpdate: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    SelectionModeTopAppBar(
        topAppBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.navigation_books)) },
                colors = colors,
                scrollBehavior = scrollBehavior
            )
        },
        actions = {
            if(selectionModeState.selectedItems.size == 1 && selectionModeState.selectedItems.first() != favoritesRecipeBookId) IconButton(
                onClick = {
                    coroutineScope.launch {
                        val book = client.container.recipeBook[
                            selectionModeState.selectedItems[0]
                        ] ?: return@launch

                        selectionModeState.disable()
                        editDialogState.open(book)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = stringResource(Res.string.action_edit)
                )
            }

            var showDeleteDialog by remember { mutableStateOf(false) }
            if(showDeleteDialog) AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = { Icon(Icons.Rounded.Delete, stringResource(Res.string.action_delete)) },
                title = {
                    Text(
                        text = pluralStringResource(
                            Res.plurals.action_delete_recipe_books,
                            quantity = selectionModeState.selectedItems.size
                        )
                    )
                },
                text = {
                    Text(
                        text = pluralStringResource(
                            Res.plurals.action_delete_recipe_books_description,
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
                    ) {
                        Text(text = stringResource(Res.string.action_abort))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false

                            coroutineScope.launch {
                                selectionModeState.selectedItems.forEach {
                                    val book = client.container.recipeBook[it] ?: return@forEach
                                    deleteRequestState.wrapRequest { book.delete() }
                                }

                                onUpdate()
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