package de.kitshn.ui.component.input.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestState
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.ui.dialog.select.SelectRecipeDialog
import de.kitshn.ui.dialog.select.rememberSelectRecipeDialogState
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.common_title_image
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseRecipeSearchField(
    client: TandoorClient,
    value: Int?,
    onValueChange: (Int?) -> Unit,
    content: @Composable (
        thumbnail: @Composable (() -> Unit)?,
        value: String,
        onClick: () -> Unit
    ) -> Unit
) {
    var selectedRecipe by remember { mutableStateOf<TandoorRecipeOverview?>(null) }

    val selectRecipeDialogState = rememberSelectRecipeDialogState()

    LaunchedEffect(value) {
        if(value == null) {
            selectedRecipe = null
            return@LaunchedEffect
        }

        if(selectedRecipe?.id != value) {
            selectedRecipe = client.container.recipeOverview[value]

            if(selectedRecipe == null) {
                TandoorRequestState().wrapRequest {
                    client.recipe.get(value).let {
                        selectedRecipe = it.toOverview()
                    }
                }
            }
        }
    }

    content(
        when(selectedRecipe != null) {
            true -> {
                {
                    AsyncImage(
                        model = selectedRecipe?.loadThumbnail(),
                        contentDescription = stringResource(Res.string.common_title_image),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }

            else -> null
        },
        selectedRecipe?.name ?: ""
    ) {
        selectRecipeDialogState.open(initialSelectedId = value)
    }

    SelectRecipeDialog(
        client = client,
        state = selectRecipeDialogState,
    ) {
        selectedRecipe = it
        onValueChange(it.id)
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
) { l, v, onClick ->
    Box {
        OutlinedTextField(
            value = v,
            modifier = modifier,
            enabled = true,
            readOnly = true,
            singleLine = true,
            textStyle = textStyle,
            label = label,
            placeholder = placeholder,
            leadingIcon = l ?: leadingIcon,
            trailingIcon = trailingIcon ?: {
                Icon(
                    imageVector = Icons.Default.UnfoldMore,
                    contentDescription = null
                )
            },
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = isError,
            shape = shape,
            colors = colors,
            onValueChange = { }
        )

        Box(
            modifier = Modifier.matchParentSize().clickable { onClick() }
        )
    }
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
) { l, v, onClick ->
    Box {
        TextField(
            value = v,
            modifier = modifier,
            enabled = true,
            readOnly = true,
            singleLine = true,
            textStyle = textStyle,
            label = label,
            placeholder = placeholder,
            leadingIcon = l ?: leadingIcon,
            trailingIcon = trailingIcon ?: {
                Icon(
                    imageVector = Icons.Default.UnfoldMore,
                    contentDescription = null
                )
            },
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = isError,
            shape = shape,
            colors = colors,
            onValueChange = { }
        )

        Box(
            modifier = Modifier.matchParentSize().clickable { onClick() }
        )
    }
}
