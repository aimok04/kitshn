package de.kitshn.ui.view.home.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarScrollBehavior
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopSearchBar
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import de.kitshn.KitshnViewModel
import de.kitshn.ui.dialog.recipe.RecipeLinkDialog
import de.kitshn.ui.dialog.recipe.rememberRecipeLinkDialogState
import de.kitshn.ui.selectionMode.model.RecipeSelectionModeTopAppBar
import de.kitshn.ui.selectionMode.rememberSelectionModeState
import de.kitshn.ui.view.ViewParameters
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_back
import kitshn.composeapp.generated.resources.action_close
import kitshn.composeapp.generated.resources.home_search_tandoor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchTopBar(
    vm: KitshnViewModel,
    state: HomeSearchState,
    scrollBehavior: SearchBarScrollBehavior
) {
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val selectionModeState = rememberSelectionModeState<Int>()

    val client = vm.tandoorClient ?: return

    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()

    LaunchedEffect(textFieldState.text) {
        delay(200)
        state.query = textFieldState.text.toString()
    }

    LaunchedEffect(searchBarState.currentValue) {
        state.shown.value = searchBarState.currentValue == SearchBarValue.Expanded
    }

    LaunchedEffect(state.shown.value) {
        if(state.shown.value)
            searchBarState.animateToExpanded()
    }

    val inputField = @Composable {
        SearchBarDefaults.InputField(
            textFieldState = textFieldState,
            searchBarState = searchBarState,
            onSearch = {
                keyboardController?.hide()
                state.query = it
            },
            placeholder = { Text(stringResource(Res.string.home_search_tandoor)) },
            leadingIcon = {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            searchBarState.animateToCollapsed()
                        }
                    }
                ) {
                    AnimatedContent(
                        targetState = searchBarState.currentValue
                    ) {
                        when(it) {
                            SearchBarValue.Expanded -> Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(Res.string.action_back)
                            )

                            else -> Icon(
                                Icons.Rounded.Search,
                                contentDescription = stringResource(Res.string.home_search_tandoor)
                            )
                        }
                    }
                }
            },

            trailingIcon = {
                AnimatedVisibility(
                    visible = searchBarState.currentValue == SearchBarValue.Expanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(onClick = {
                        textFieldState.edit { delete(0, originalText.length) }
                        state.additionalSearchSettingsChipRowState.reset()
                    }) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = stringResource(Res.string.action_close)
                        )
                    }
                }
            }
        )
    }

    val recipeLinkDialogState = rememberRecipeLinkDialogState()

    TopSearchBar(
        state = searchBarState,
        inputField = inputField,
        scrollBehavior = scrollBehavior
    )

    ExpandedFullScreenSearchBar(
        state = searchBarState,
        inputField = inputField
    ) {
        Box {
            ViewHomeSearchContent(
                client = client,
                state = state,
                selectionModeState = selectionModeState,
                onClick = {
                    recipeLinkDialogState.open(it)
                }
            )

            RecipeSelectionModeTopAppBar(
                vm = vm,
                topAppBar = { },
                state = selectionModeState
            )
        }
    }

    RecipeLinkDialog(
        p = ViewParameters(
            vm = vm,
            back = {
                recipeLinkDialogState.dismiss()
            }
        ),
        state = recipeLinkDialogState
    )
}