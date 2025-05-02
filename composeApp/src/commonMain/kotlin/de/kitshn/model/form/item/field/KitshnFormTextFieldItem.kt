package de.kitshn.model.form.item.field

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.form_error_field_empty
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class KitshnFormTextFieldItem(
    val value: () -> String,
    val onValueChange: (value: String) -> Unit,

    label: @Composable () -> Unit,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,

    optional: Boolean = false,

    val visualTransformation: VisualTransformation = VisualTransformation.None,
    val singleLine: Boolean = false,
    val maxLines: Int = if(singleLine) 1 else Int.MAX_VALUE,
    val minLines: Int = 1,

    val check: suspend (data: String) -> String?
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
    override fun Render(
        modifier: Modifier
    ) {
        val focusManager = LocalFocusManager.current

        var error by rememberSaveable { mutableStateOf<String?>(null) }
        val value = value()

        val coroutineScope = rememberCoroutineScope()

        TextField(
            modifier = modifier,

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

            visualTransformation = visualTransformation,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,

            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            keyboardOptions = KeyboardOptions(
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
                coroutineScope.launch {
                    generalError = null
                    onValueChange(it)

                    error = if(it.isBlank())
                        null
                    else
                        check(it)
                }
            }
        )
    }

    override suspend fun submit(): Boolean {
        val value = value()
        val checkResult = check(value)

        if(!optional && value.isBlank()) {
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