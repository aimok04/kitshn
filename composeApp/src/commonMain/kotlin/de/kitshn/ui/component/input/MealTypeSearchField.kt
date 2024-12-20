package de.kitshn.ui.component.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.api.tandoor.model.TandoorMealType
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_unknown_meal_type
import org.jetbrains.compose.resources.getString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseMealTypeSearchField(
    client: TandoorClient,
    value: Int?,
    onValueChange: (Int?) -> Unit,
    content: @Composable ExposedDropdownMenuBoxScope.(
        thumbnail: @Composable (() -> Unit)?,
        value: String,
        onValueChange: (value: String) -> Unit
    ) -> Unit
) {
    val focus = LocalFocusManager.current

    var selectedMealType by remember { mutableStateOf<TandoorMealType?>(null) }
    LaunchedEffect(selectedMealType) { onValueChange(selectedMealType?.id) }

    var isExpanded by remember { mutableStateOf(false) }

    var searchText by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(value) {
        if(value == null) return@LaunchedEffect
        if(selectedMealType?.id != value) selectedMealType = client.container.mealType[value]

        searchText = selectedMealType?.name ?: getString(Res.string.common_unknown_meal_type)
    }

    val mealTypeList = remember { mutableStateListOf<TandoorMealType>() }
    LaunchedEffect(Unit) {
        try {
            client.mealType.fetch().let {
                mealTypeList.clear()
                mealTypeList.addAll(it)
            }
        } catch(e: TandoorRequestsError) {
            e.printStackTrace()
        }
    }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = {
            isExpanded = it
            focus.clearFocus()
        }
    ) {
        content(
            if(selectedMealType != null) {
                {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(selectedMealType!!.color)
                    )
                }
            } else null,
            searchText
        ) {
            searchText = it
            selectedMealType = null
        }

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = {
                isExpanded = false
                focus.clearFocus()
            }
        ) {
            mealTypeList.forEach {
                DropdownMenuItem(
                    text = { Text(it.name) },
                    onClick = {
                        selectedMealType = it
                        isExpanded = false

                        focus.clearFocus()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinedMealTypeSearchField(
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
) = BaseMealTypeSearchField(
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
fun MealTypeSearchField(
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
) = BaseMealTypeSearchField(
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