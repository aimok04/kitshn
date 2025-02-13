package de.kitshn.ui.component.input

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestState
import de.kitshn.api.tandoor.model.TandoorFood
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.ui.TandoorRequestErrorHandler
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseFoodSearchField(
    client: TandoorClient,
    modifier: Modifier = Modifier,
    dropdownMenuModifier: Modifier = Modifier,
    value: String?,
    onValueChange: (String?) -> Unit,
    onSelect: () -> Unit,
    content: @Composable (
        modifier: Modifier,
        value: String,
        onValueChange: (value: String) -> Unit
    ) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    var searchText by rememberSaveable { mutableStateOf("") }
    var fromDropdown by remember { mutableStateOf(false) }

    val foodList = remember { mutableStateListOf<TandoorFood>() }

    val searchRequestState = rememberTandoorRequestState()
    LaunchedEffect(searchText) {
        foodList.clear()
        if(searchText.isEmpty()) return@LaunchedEffect
        if(fromDropdown) return@LaunchedEffect

        delay(300)

        searchRequestState.wrapRequest {
            TandoorRequestState().wrapRequest {
                client.food.list(
                    query = searchText,
                    pageSize = 3
                ).results.let {
                    foodList.clear()
                    foodList.addAll(it)

                    isExpanded = true
                }
            }
        }
    }

    ExposedDropdownMenuBox(
        modifier = dropdownMenuModifier,
        expanded = isExpanded,
        onExpandedChange = {
            isExpanded = it
        }
    ) {
        content(
            modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true)
                .onFocusChanged { if(!it.isFocused) isExpanded = false },
            value ?: searchText
        ) {
            fromDropdown = false
            searchText = it
            onValueChange(it)
        }

        if(foodList.size > 0) ExposedDropdownMenu(
            modifier = Modifier.exposedDropdownSize(true),
            expanded = isExpanded,
            onDismissRequest = {
                isExpanded = false
            }
        ) {
            foodList.forEach {
                DropdownMenuItem(
                    text = { Text(it.name) },
                    onClick = {
                        fromDropdown = true
                        searchText = it.name
                        onValueChange(it.name)

                        isExpanded = false
                        onSelect()
                    }
                )
            }
        }
    }

    TandoorRequestErrorHandler(state = searchRequestState)
}

@Composable
fun OutlinedFoodSearchField(
    client: TandoorClient,
    value: String?,
    onValueChange: (String?) -> Unit,
    onSelect: () -> Unit,
    dropdownMenuModifier: Modifier,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) = BaseFoodSearchField(
    client = client,
    modifier = modifier,
    onSelect = onSelect,
    dropdownMenuModifier = dropdownMenuModifier,
    value = value,
    onValueChange = onValueChange
) { mdf, v, vc ->
    OutlinedTextField(
        value = v,
        modifier = mdf,
        enabled = true,
        readOnly = false,
        singleLine = true,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        isError = isError,
        shape = shape,
        colors = colors,
        onValueChange = vc
    )
}

@Composable
fun FoodSearchField(
    client: TandoorClient,
    value: String?,
    onValueChange: (String?) -> Unit,
    onSelect: () -> Unit,
    dropdownMenuModifier: Modifier,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors()
) = BaseFoodSearchField(
    client = client,
    modifier = modifier,
    onSelect = onSelect,
    dropdownMenuModifier = dropdownMenuModifier,
    value = value,
    onValueChange = onValueChange
) { mdf, v, vc ->
    TextField(
        value = v,
        modifier = mdf,
        enabled = true,
        readOnly = false,
        singleLine = true,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        isError = isError,
        shape = shape,
        colors = colors,
        onValueChange = vc
    )
}