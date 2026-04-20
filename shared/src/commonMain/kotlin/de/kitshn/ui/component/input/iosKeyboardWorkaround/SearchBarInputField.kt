package de.kitshn.ui.component.input.iosKeyboardWorkaround

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay

// Hides keyboard on iOS when performing search action
// https://github.com/aimok04/kitshn/issues/312 and https://github.com/aimok04/kitshn/issues/317

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarDefaults.InputFieldWithIOSKeyboardWorkaround(
    textFieldState: TextFieldState,
    searchBarState: SearchBarState,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    scrollState: ScrollState = rememberScrollState(),
    shape: Shape = inputFieldShape,
    colors: TextFieldColors = inputFieldColors(),
    interactionSource: MutableInteractionSource? = null,
) {
    var enableTextField by remember { mutableStateOf(true) }
    LaunchedEffect(enableTextField) {
        delay(1000)
        enableTextField = true
    }

    InputField(
        textFieldState = textFieldState,
        searchBarState = searchBarState,
        onSearch = {
            enableTextField = false
            onSearch(it)
        },
        modifier = modifier,
        enabled = enableTextField && enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon =  trailingIcon,
        prefix = prefix,
        suffix = suffix,
        inputTransformation = inputTransformation,
        outputTransformation = outputTransformation,
        keyboardOptions = keyboardOptions,
        lineLimits = lineLimits,
        scrollState = scrollState,
        shape = shape,
        colors = colors,
        interactionSource = interactionSource
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarDefaults.InputFieldWithIOSKeyboardWorkaround(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
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
    var enableTextField by remember { mutableStateOf(true) }
    LaunchedEffect(enableTextField) {
        delay(1000)
        enableTextField = true
    }

    InputField(
        query = query,
        onQueryChange = onQueryChange,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        onSearch = {
            enableTextField = false
            onSearch(it)
        },
        modifier = modifier,
        enabled = enableTextField && enabled,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon =  trailingIcon,
        colors = colors,
        interactionSource = interactionSource
    )
}