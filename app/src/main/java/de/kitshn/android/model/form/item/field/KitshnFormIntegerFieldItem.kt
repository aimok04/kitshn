package de.kitshn.android.model.form.item.field

import android.content.Context
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.kitshn.android.R
import de.kitshn.android.ui.component.input.NumberField

class KitshnFormIntegerFieldItem(
    val value: () -> Int?,
    val onValueChange: (value: Int?) -> Unit,

    label: @Composable () -> Unit,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,

    val min: () -> Int? = { null },
    val max: () -> Int? = { null },

    optional: Boolean = false,

    val check: (value: Int?) -> String?
) : KitshnFormBaseFieldItem(
    label = label,
    placeholder = placeholder,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    prefix = prefix,
    suffix = suffix,
    optional = optional
) {

    var context: Context? = null

    @Composable
    override fun Render() {
        val focusManager = LocalFocusManager.current

        var error by rememberSaveable { mutableStateOf<String?>(null) }
        val value = value()

        context = LocalContext.current

        fun checkValueChange(it: Int?) {
            generalError = null
            onValueChange(it)

            error = if(it == null)
                null
            else
                check(it)
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberField(
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

                        val newValue = value?.let { it - 1 } ?: min() ?: 1
                        if(min != null && newValue < min) return@SmallFloatingActionButton

                        focusManager.clearFocus(force = true)
                        checkValueChange(newValue)
                    }
                ) {
                    Icon(Icons.Rounded.Remove, stringResource(R.string.action_minus))
                }

                SmallFloatingActionButton(
                    modifier = Modifier
                        .padding(start = 4.dp),
                    onClick = {
                        val max = max()

                        val newValue = value?.let { it + 1 } ?: min() ?: 1
                        if(max != null && newValue > max) return@SmallFloatingActionButton

                        focusManager.clearFocus(force = true)
                        checkValueChange(newValue)
                    }
                ) {
                    Icon(Icons.Rounded.Add, stringResource(R.string.action_plus))
                }
            }
        }
    }

    override fun submit(): Boolean {
        val value = value()
        val checkResult = check(value)

        if(!optional && value == null) {
            generalError = context?.getString(R.string.form_error_field_empty)
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