package de.kitshn.ui.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.ui.component.input.iosKeyboardWorkaround.InputFieldWithIOSKeyboardWorkaround
import de.kitshn.ui.component.search.RecipeSearchContent
import de.kitshn.ui.component.search.RecipeSearchState
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_close
import kitshn.composeapp.generated.resources.home_search_tandoor
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeSearchDialog(
    client: TandoorClient,
    onDismissRequest: () -> Unit,
    initialSelectedId: Int? = null,
    title: String? = null,
    placeholder: String? = null,
    onSelect: (TandoorRecipeOverview) -> Unit
) {
    val state = remember { RecipeSearchState() }
    LaunchedEffect(Unit) {
        state.open(initialSelectedId = initialSelectedId)
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    val textFieldState = rememberTextFieldState(initialText = state.query)
    val searchBarState = rememberSearchBarState(
        initialValue = SearchBarValue.Expanded
    )

    // query debounce
    LaunchedEffect(textFieldState.text) {
        // the content debounces this
        state.query = textFieldState.text.toString()
    }

    var canFocusWorkaround by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(600)
        canFocusWorkaround = true
        focusRequester.requestFocus()
    }

    val inputField = @Composable {
        SearchBarDefaults.InputFieldWithIOSKeyboardWorkaround(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .focusProperties {
                    canFocus = canFocusWorkaround
                },
            textFieldState = textFieldState,
            searchBarState = searchBarState,
            onSearch = {
                keyboardController?.hide()
            },
            placeholder = { Text(placeholder ?: stringResource(Res.string.home_search_tandoor)) },
            trailingIcon = {
                AnimatedVisibility(
                    visible = textFieldState.text.isNotEmpty(),
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

    AdaptiveFullscreenDialog(
        onDismiss = {
            onDismissRequest()
        },
        title = {
            title?.let { Text(it) }
        }
    ) { _, isFullscreen, _ ->
        Column(
            Modifier
                .fillMaxSize()
        ) {
            if(title != null) {
                Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    inputField()
                }
            }

            RecipeSearchContent(
                client = client,
                state = state,
                listItemColors = ListItemDefaults.colors(
                    containerColor = when(isFullscreen) {
                        true -> MaterialTheme.colorScheme.surfaceContainerLow
                        false -> MaterialTheme.colorScheme.surfaceContainerHighest
                    }
                ),
                onClick = onSelect
            )
        }
    }
}