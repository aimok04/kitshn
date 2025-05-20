package de.kitshn.ui.view.home.search

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarDefaults.InputFieldHeight
import androidx.compose.material3.SearchBarDefaults.inputFieldColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import de.kitshn.BackHandler
import de.kitshn.KitshnViewModel
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.ui.selectionMode.model.RecipeSelectionModeTopAppBar
import de.kitshn.ui.selectionMode.rememberSelectionModeState
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_back
import kitshn.composeapp.generated.resources.action_close
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

    var query by remember {
        mutableStateOf(
            TextFieldValue(
                text = state.query,
                selection = TextRange(state.query.length)
            )
        )
    }

    LaunchedEffect(query) {
        delay(200)
        state.query = query.text
    }

    if(state.shown.value) SearchBar(
        inputField = {
            SearchInputField(
                modifier = Modifier.focusRequester(focusRequester),
                query = query,
                onQueryChange = {
                    query = it
                },
                onSearch = {
                    keyboardController?.hide()
                    state.query = it.text
                },
                expanded = true,
                onExpandedChange = { },
                placeholder = { Text(stringResource(Res.string.home_search_tandoor)) },
                leadingIcon = {
                    IconButton(onClick = {
                        state.dismiss()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(Res.string.action_back)
                        )
                    }
                },
                trailingIcon = {
                    IconButton(onClick = {
                        query = TextFieldValue("")
                        focusRequester.requestFocus()
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

            delay(100)
            focusRequester.requestFocus()
            state.appliedAutoFocusSearchField = true
        } catch(e: Exception) {
            Logger.e("HomeSearch.kt", e)
        }
    }
}


/**
 * Copy of SearchBarDefaults.InputField, replacing "String" with "TextFieldValue" to allow setting "selection" value.
 */
@ExperimentalMaterial3Api
@Composable
fun SearchInputField(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onSearch: (TextFieldValue) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    colors: TextFieldColors = inputFieldColors(),
    interactionSource: MutableInteractionSource? = null,
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }

    val focused = interactionSource.collectIsFocusedAsState().value
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val textColor =
        LocalTextStyle.current.color.takeOrElse {
            colors.textColor(enabled, isError = false, focused = focused)
        }

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier =
            modifier
                .sizeIn(
                    minWidth = 360.dp,
                    maxWidth = 720.dp,
                    minHeight = InputFieldHeight,
                )
                .focusRequester(focusRequester)
                .onFocusChanged { if (it.isFocused) onExpandedChange(true) }
                .semantics {
                    onClick {
                        focusRequester.requestFocus()
                        true
                    }
                },
        enabled = enabled,
        singleLine = true,
        textStyle = LocalTextStyle.current.merge(TextStyle(color = textColor)),
        cursorBrush = SolidColor(colors.cursorColor(isError = false)),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
        interactionSource = interactionSource,
        decorationBox =
            @Composable { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = query.text,
                    innerTextField = innerTextField,
                    enabled = enabled,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    placeholder = placeholder,
                    leadingIcon =
                        leadingIcon?.let { leading ->
                            { Box(Modifier.offset(x = 4.dp)) { leading() } }
                        },
                    trailingIcon =
                        trailingIcon?.let { trailing ->
                            { Box(Modifier.offset(x = (-4).dp)) { trailing() } }
                        },
                    shape = SearchBarDefaults.inputFieldShape,
                    colors = colors,
                    contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(),
                    container = {},
                )
            }
    )

    val shouldClearFocus = !expanded && focused
    LaunchedEffect(expanded) {
        if (shouldClearFocus) {
            // Not strictly needed according to the motion spec, but since the animation
            // already has a delay, this works around b/261632544.
            delay(100.0.toLong())
            focusManager.clearFocus()
        }
    }
}

@Stable
internal fun TextFieldColors.textColor(
    enabled: Boolean,
    isError: Boolean,
    focused: Boolean,
): Color =
    when {
        !enabled -> disabledTextColor
        isError -> errorTextColor
        focused -> focusedTextColor
        else -> unfocusedTextColor
    }

@Stable
internal fun TextFieldColors.cursorColor(isError: Boolean): Color =
    if (isError) errorCursorColor else cursorColor