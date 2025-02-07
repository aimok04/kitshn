package de.kitshn.ui.component.input.shopping

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarketCategory
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.ui.TandoorRequestErrorHandler
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_add
import kitshn.composeapp.generated.resources.common_create_argument
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseCategorySearchField(
    modifier: Modifier,
    client: TandoorClient,
    value: TandoorSupermarketCategory?,
    onValueChange: (TandoorSupermarketCategory?) -> Unit,
    content: @Composable ExposedDropdownMenuBoxScope.(
        modifier: Modifier,
        value: String,
        onValueChange: (value: String) -> Unit
    ) -> Unit
) {
    val density = LocalDensity.current

    var isExpanded by remember { mutableStateOf(false) }
    val fetchRequestState = rememberTandoorRequestState()

    var searchText by remember { mutableStateOf("") }
    LaunchedEffect(value) { searchText = value?.name ?: "" }

    val categoriesList = remember { mutableStateListOf<TandoorSupermarketCategory>() }
    LaunchedEffect(Unit) {
        fetchRequestState.wrapRequest {
            categoriesList.addAll(
                client.supermarket.fetchCategories()
            )
        }
    }

    val matchingCategoriesList = remember { mutableStateListOf<TandoorSupermarketCategory>() }
    LaunchedEffect(searchText) {
        matchingCategoriesList.clear()

        categoriesList.forEach {
            if(matchingCategoriesList.size >= 5) return@LaunchedEffect
            if(!it.name.lowercase().contains(searchText.lowercase())) return@forEach
            matchingCategoriesList.add(it)
        }
    }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = {
            isExpanded = it
        }
    ) {
        content(modifier, searchText) {
            searchText = it
        }

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = {
                isExpanded = false
            }
        ) {
            matchingCategoriesList.forEach {
                DropdownMenuItem(
                    text = { Text(it.name) },
                    onClick = {
                        onValueChange(it)

                        isExpanded = false
                    }
                )
            }

            if(searchText.isNotEmpty() && categoriesList.find { it.name.lowercase() == searchText.lowercase() } == null) DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Rounded.Add, stringResource(Res.string.action_add))
                },
                text = { Text(stringResource(Res.string.common_create_argument, searchText)) },
                onClick = {
                    onValueChange(
                        TandoorSupermarketCategory(
                            id = null,
                            name = searchText
                        )
                    )

                    isExpanded = false
                }
            )
        }

        // keep dropdown menu on top of input
        Box(
            Modifier.height(
                with(density) {
                    WindowInsets.ime.getBottom(LocalDensity.current).toDp()
                }
            )
        )
    }

    TandoorRequestErrorHandler(state = fetchRequestState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinedCategorySearchField(
    client: TandoorClient,
    value: TandoorSupermarketCategory?,
    onValueChange: (TandoorSupermarketCategory?) -> Unit,
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
) = BaseCategorySearchField(
    modifier = modifier,
    client = client,
    value = value,
    onValueChange = onValueChange
) { mdf, v, vc ->
    OutlinedTextField(
        value = v,
        modifier = mdf.menuAnchor(MenuAnchorType.PrimaryEditable, true),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySearchField(
    client: TandoorClient,
    value: TandoorSupermarketCategory?,
    onValueChange: (TandoorSupermarketCategory?) -> Unit,
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
) = BaseCategorySearchField(
    modifier = modifier,
    client = client,
    value = value,
    onValueChange = onValueChange
) { mdf, v, vc ->
    TextField(
        value = v,
        modifier = mdf.menuAnchor(MenuAnchorType.PrimaryEditable, true),
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