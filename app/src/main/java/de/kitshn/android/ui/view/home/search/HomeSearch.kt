package de.kitshn.android.ui.view.home.search

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.res.stringResource
import de.kitshn.android.KitshnViewModel
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.android.ui.selectionMode.model.RecipeSelectionModeTopAppBar
import de.kitshn.android.ui.selectionMode.rememberSelectionModeState
import kotlinx.coroutines.delay

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
                placeholder = { Text(stringResource(R.string.home_search_tandoor)) },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = stringResource(id = R.string.common_search)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = {
                        state.dismiss()
                    }) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = stringResource(id = R.string.action_close)
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

        // needed because system back is consumed by most inner handler
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