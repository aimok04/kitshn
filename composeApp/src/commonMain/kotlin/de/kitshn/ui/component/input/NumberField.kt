package de.kitshn.ui.component.input

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import co.touchlab.kermit.Logger

@Composable
fun BaseNumberField(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    min: Int? = null,
    max: Int? = null,
    content: @Composable (
        value: String,
        interactionSource: MutableInteractionSource,
        onValueChanged: (value: String) -> Unit
    ) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    var uiValue by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(isFocused, value) {
        if(isFocused) return@LaunchedEffect
        uiValue = value?.toString() ?: ""
    }

    content(
        uiValue,
        interactionSource
    ) {
        uiValue = it.filter { c -> c.isDigit() }

        if(it.isBlank()) {
            onValueChange(null)
        } else {
            try {
                val newValue = uiValue.toInt()
                if(min != null && newValue < min) return@content
                if(max != null && newValue > max) return@content

                onValueChange(newValue)
            } catch(e: Exception) {
                Logger.e("NumberField.kt", e)
            }
        }
    }
}

@Composable
fun OutlinedNumberField(
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
    min: Int? = null,
    max: Int? = null,
    supportingText: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) = BaseNumberField(
    value = value,
    min = min,
    max = max,
    onValueChange = onValueChange
) { v, iso, vc ->
    OutlinedTextField(
        value = v,
        modifier = modifier,
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
        interactionSource = iso,
        onValueChange = vc
    )
}

@Composable
fun NumberField(
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
    min: Int? = null,
    max: Int? = null,
    supportingText: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors()
) = BaseNumberField(
    value = value,
    min = min,
    max = max,
    onValueChange = onValueChange
) { v, iso, v2 ->
    TextField(
        value = v,
        modifier = modifier,
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
        interactionSource = iso,
        onValueChange = v2
    )
}