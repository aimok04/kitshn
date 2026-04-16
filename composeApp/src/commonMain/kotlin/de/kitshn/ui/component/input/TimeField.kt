package de.kitshn.ui.component.input

import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TimePickerDialogDefaults
import androidx.compose.material3.TimePickerDisplayMode
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_abort
import kitshn.composeapp.generated.resources.common_okay
import kitshn.composeapp.generated.resources.common_time
import org.jetbrains.compose.resources.stringResource
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseTimeField(
    value: LocalTime?,
    onValueChange: (LocalTime?) -> Unit,
    content: @Composable (
        value: String,
        onClick: () -> Unit
    ) -> Unit
) {
    var showTimePickerDialog by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour = value?.hour ?: 0,
        initialMinute = value?.minute ?: 0,
        is24Hour = true,
    )

    content(
        value?.toString()?.substring(0, 5) ?: ""
    ) {
        showTimePickerDialog = true
    }

    if (showTimePickerDialog) {
        var displayMode by remember { mutableStateOf(TimePickerDisplayMode.Picker) }

        TimePickerDialog(
            onDismissRequest = { showTimePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onValueChange(LocalTime(timePickerState.hour, timePickerState.minute))
                        showTimePickerDialog = false
                    }
                ) {
                    Text(stringResource(Res.string.common_okay))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTimePickerDialog = false }
                ) {
                    Text(stringResource(Res.string.action_abort))
                }
            },
            title = {
                Text(stringResource(Res.string.common_time))
            },
            modeToggleButton = {
                TimePickerDialogDefaults.DisplayModeToggle(
                    onDisplayModeChange = {
                        displayMode = if (displayMode == TimePickerDisplayMode.Picker) {
                            TimePickerDisplayMode.Input
                        } else {
                            TimePickerDisplayMode.Picker
                        }
                    },
                    displayMode = displayMode
                )
            }
        ) {
            if (displayMode == TimePickerDisplayMode.Input) {
                TimeInput(state = timePickerState)
            } else {
                TimePicker(state = timePickerState)
            }
        }
    }
}

@Composable
fun OutlinedTimeField(
    value: LocalTime?,
    onValueChange: (LocalTime?) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) = BaseTimeField(
    value = value,
    onValueChange = onValueChange
) { v, onClick ->
    OutlinedTextField(
        value = v,
        modifier = modifier,
        enabled = true,
        readOnly = true,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        shape = shape,
        colors = colors,
        interactionSource = remember { MutableInteractionSource() }
            .also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect {
                        if(it !is FocusInteraction.Focus && it !is PressInteraction.Release) return@collect
                        onClick()
                    }
                }
            },
        onValueChange = { }
    )
}

@Composable
fun TimeField(
    value: LocalTime?,
    onValueChange: (LocalTime?) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors()
) = BaseTimeField(
    value = value,
    onValueChange = onValueChange
) { v, onClick ->
    TextField(
        value = v,
        modifier = modifier,
        enabled = true,
        readOnly = true,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        shape = shape,
        colors = colors,
        interactionSource = remember { MutableInteractionSource() }
            .also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect {
                        if(it !is FocusInteraction.Focus && it !is PressInteraction.Release) return@collect
                        onClick()
                    }
                }
            },
        onValueChange = { }
    )
}
