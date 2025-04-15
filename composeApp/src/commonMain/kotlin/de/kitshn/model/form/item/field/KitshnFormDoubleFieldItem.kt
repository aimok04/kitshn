package de.kitshn.model.form.item.field

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.kitshn.ui.component.input.DoubleField
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_minus
import kitshn.composeapp.generated.resources.action_plus
import kitshn.composeapp.generated.resources.form_error_field_empty
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

class KitshnFormDoubleFieldItem(
    val value: () -> Double?,
    val onValueChange: (value: Double?) -> Unit,

    label: @Composable () -> Unit,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,

    val min: () -> Double? = { null },
    val max: () -> Double? = { null },

    optional: Boolean = false,

    val check: suspend (value: Double?) -> String?
) : KitshnFormBaseFieldItem(
    label = label,
    placeholder = placeholder,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    prefix = prefix,
    suffix = suffix,
    optional = optional
) {

    @Composable
    override fun Render() {
        val focusManager = LocalFocusManager.current

        var error by rememberSaveable { mutableStateOf<String?>(null) }
        val value = value()

        val coroutineScope = rememberCoroutineScope()

        fun checkValueChange(it: Double?) {
            coroutineScope.launch {
                generalError = null
                onValueChange(it)

                error = if(it == null)
                    null
                else
                    check(it)
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DoubleField(
                modifier = Modifier.weight(1.2f, true),
                value = value,
                label = {
                    Row {
                        label()
                        if(!optional) Text("*")
                    }
                },

                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                prefix = prefix,
                suffix = suffix,

                min = min(),
                max = max(),

                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),

                isError = error != null || generalError != null,
                supportingText = if(error != null || generalError != null) {
                    {
                        Text(
                            text = error ?: generalError ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else null,

                onValueChange = {
                    checkValueChange(it)
                }
            )

            Row {
                SmallFloatingActionButton(
                    modifier = Modifier
                        .padding(start = 8.dp),
                    onClick = {
                        val min = min()

                        val newValue = value?.let { it - 1 } ?: min() ?: 1.0
                        if(min != null && newValue < min) return@SmallFloatingActionButton

                        focusManager.clearFocus(force = true)
                        checkValueChange(newValue)
                    }
                ) {
                    Icon(Icons.Rounded.Remove, stringResource(Res.string.action_minus))
                }

                SmallFloatingActionButton(
                    modifier = Modifier
                        .padding(start = 4.dp),
                    onClick = {
                        val max = max()

                        val newValue = value?.let { it + 1 } ?: min() ?: 1.0
                        if(max != null && newValue > max) return@SmallFloatingActionButton

                        focusManager.clearFocus(force = true)
                        checkValueChange(newValue)
                    }
                ) {
                    Icon(Icons.Rounded.Add, stringResource(Res.string.action_plus))
                }
            }
        }
    }

    override suspend fun submit(): Boolean {
        val value = value()
        val checkResult = check(value)

        if(!optional && value == null) {
            generalError = getString(Res.string.form_error_field_empty)
            return false
        } else if(checkResult != null) {
            generalError = checkResult
            return false
        } else {
            generalError = null
            return true
        }
    }

}