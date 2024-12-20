package de.kitshn.ui.view.home.search

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import de.kitshn.BackHandler
import de.kitshn.KitshnViewModel
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.ui.selectionMode.model.RecipeSelectionModeTopAppBar
import de.kitshn.ui.selectionMode.rememberSelectionModeState
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_close
import kitshn.composeapp.generated.resources.common_search
import kitshn.composeapp.generated.resources.home_search_tandoor
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewHomeSearch(
    vm: KitshnViewModel,
    state: HomeSearchState,
    handleBack: Boolean = false,
    onBack: () -> Unit = {},
    onClick: (recipe: TandoorRecipeOverview) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    val selectionModeState = rememberSelectionModeState<Int>()

    val client = vm.tandoorClient ?: return

    // handle keyword passing
    vm.uiState.searchKeyword.WatchAndConsume {
        state.openWithKeywordId(client, it)
    }

    var query by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(query) {
        delay(200)
        state.query = query
    }

    LaunchedEffect(state.shown.value) {
        query = state.defaultValues.query
        state.query = state.defaultValues.query
    }

    if(state.shown.value) SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                modifier = Modifier.focusRequester(focusRequester),
                query = query,
                onQueryChange = {
                    query = it
                },
                onSearch = {
                    keyboardController?.hide()
                    state.query = it
                },
                expanded = true,
                onExpandedChange = { },
                placeholder = { Text(stringResource(Res.string.home_search_tandoor)) },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = stringResource(Res.string.common_search)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = {
                        state.dismiss()
                    }) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = stringResource(Res.string.action_close)
                        )
                    }
                }
            )
        },
        expanded = true,
        onExpandedChange = { state.dismiss() },
    ) {
        Box {
            ViewHomeSearchContent(
                client = client,
                state = state,
                selectionModeState = selectionModeState,
                onClick = onClick
            )

            RecipeSelectionModeTopAppBar(
                vm = vm,
                topAppBar = { },
                state = selectionModeState
            )
        }

        BackHandler(
            handleBack,
            onBack
        )
    }

    if(state.defaultValues.autoFocusSearchField) LaunchedEffect(state.shown.value) {
        try {
            if(!state.shown.value || state.appliedAutoFocusSearchField) return@LaunchedEffect

            focusRequester.requestFocus()
            state.appliedAutoFocusSearchField = true
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }
}