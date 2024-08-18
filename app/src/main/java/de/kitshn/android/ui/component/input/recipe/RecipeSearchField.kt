package de.kitshn.android.ui.component.input.recipe

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.TandoorRequestState
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.android.api.tandoor.route.TandoorRecipeQueryParameters
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseRecipeSearchField(
    client: TandoorClient,
    value: Int?,
    onValueChange: (Int?) -> Unit,
    content: @Composable ExposedDropdownMenuBoxScope.(
        thumbnail: @Composable (() -> Unit)?,
        value: String,
        onValueChange: (value: String) -> Unit
    ) -> Unit
) {
    val context = LocalContext.current

    var selectedRecipe by remember { mutableStateOf<TandoorRecipeOverview?>(null) }
    LaunchedEffect(selectedRecipe) { onValueChange(selectedRecipe?.id) }

    var isExpanded by remember { mutableStateOf(false) }

    var searchText by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(value) {
        if(value == null) return@LaunchedEffect
        if(selectedRecipe?.id != value) selectedRecipe = client.container.recipeOverview[value]

        searchText = selectedRecipe?.name ?: context.getString(R.string.common_unknown_recipe)
    }

    val recipeOverviewList = remember { mutableStateListOf<TandoorRecipeOverview>() }
    LaunchedEffect(searchText) {
        delay(300)

        TandoorRequestState().wrapRequest {
            client.recipe.list(
                parameters = TandoorRecipeQueryParameters(query = searchText),
                pageSize = 5
            ).results.let {
                recipeOverviewList.clear()
                recipeOverviewList.addAll(it)
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = {
            isExpanded = it
        }
    ) {
        content(
            if(selectedRecipe != null) {
                {
                    AsyncImage(
                        model = selectedRecipe?.loadThumbnail(),
                        contentDescription = stringResource(R.string.common_title_image),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            } else null,
            searchText
        ) {
            searchText = it
            selectedRecipe = null
        }

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = {
                isExpanded = false
            }
        ) {
            recipeOverviewList.forEach {
                DropdownMenuItem(
                    text = { Text(it.name) },
                    onClick = {
                        selectedRecipe = it
                        isExpanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinedRecipeSearchField(
    client: TandoorClient,
    value: Int?,
    onValueChange: (Int?) -> Unit,
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
) = BaseRecipeSearchField(
    client = client,
    value = value,
    onValueChange = onValueChange
) { t, v, vc ->
    OutlinedTextField(
        value = v,
        modifier = modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true),
        enabled = true,
        readOnly = false,
        singleLine = true,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = t ?: leadingIcon,
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
fun RecipeSearchField(
    client: TandoorClient,
    value: Int?,
    onValueChange: (Int?) -> Unit,
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
) = BaseRecipeSearchField(
    client = client,
    value = value,
    onValueChange = onValueChange
) { t, v, vc ->
    TextField(
        value = v,
        modifier = modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true),
        enabled = true,
        readOnly = false,
        singleLine = true,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = t ?: leadingIcon,
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