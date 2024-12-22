package de.kitshn.ui.component.input

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import de.kitshn.toHumanReadableDateLabel
import de.kitshn.toLocalDate
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_okay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseDateField(
    value: LocalDate?,
    onValueChange: (LocalDate?) -> Unit,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    content: @Composable (
        value: String,
        onClick: () -> Unit
    ) -> Unit
) {
    val todayEpochDays = remember {
        (Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toEpochDays())
    }

    var minDateEpochDays by rememberSaveable { mutableIntStateOf(0) }
    var maxDateEpochDays by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(minDate) {
        minDateEpochDays = minDate?.toEpochDays() ?: 0

        if(minDate == null || value == null) return@LaunchedEffect
        if(value < minDate) onValueChange(minDate)
    }

    LaunchedEffect(maxDate) {
        maxDateEpochDays = maxDate?.toEpochDays() ?: 0

        if(maxDate == null || value == null) return@LaunchedEffect
        if(value > maxDate) onValueChange(minDate)
    }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(selectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            val epochDays = Instant.fromEpochMilliseconds(utcTimeMillis)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date.toEpochDays()

            if(utcTimeMillis < todayEpochDays) return false
            if(minDateEpochDays != 0 && epochDays < minDateEpochDays) return false
            if(maxDateEpochDays != 0 && epochDays > maxDateEpochDays) return false

            return true
        }
    })

    content(
        value?.toHumanReadableDateLabel() ?: ""
    ) {
        showDatePickerDialog = true
    }

    if(showDatePickerDialog) DatePickerDialog(
        onDismissRequest = { showDatePickerDialog = false },
        confirmButton = {
            Button(onClick = {
                showDatePickerDialog = false
                onValueChange(datePickerState.selectedDateMillis?.toLocalDate())
            }) {
                Text(stringResource(Res.string.common_okay))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun OutlinedDateField(
    value: LocalDate?,
    onValueChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) = BaseDateField(
    value = value,
    minDate = minDate,
    maxDate = maxDate,
    onValueChange = onValueChange
) { v, onClick ->
    val source = remember {
        MutableInteractionSource()
    }

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
        interactionSource = source,
        onValueChange = { }
    )

    if(!source.collectIsPressedAsState().value) return@BaseDateField
    onClick()
}

@Composable
fun DateField(
    value: LocalDate?,
    onValueChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors()
) = BaseDateField(
    value = value,
    minDate = minDate,
    maxDate = maxDate,
    onValueChange = onValueChange
) { v, onClick ->
    val source = remember {
        MutableInteractionSource()
    }

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
        interactionSource = source,
        onValueChange = { }
    )

    if(!source.collectIsPressedAsState().value) return@BaseDateField
    onClick()
}