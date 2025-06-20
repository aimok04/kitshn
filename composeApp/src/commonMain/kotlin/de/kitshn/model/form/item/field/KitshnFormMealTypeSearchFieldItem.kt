package de.kitshn.model.form.item.field

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.ui.component.input.MealTypeSearchField
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.form_error_field_empty
import org.jetbrains.compose.resources.getString

class KitshnFormMealTypeSearchFieldItem(
    val client: TandoorClient,
    val value: () -> Int?,
    val onValueChange: (value: Int?) -> Unit,

    label: @Composable () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,

    optional: Boolean = false,

    val check: (data: Int?) -> String?
) : KitshnFormBaseFieldItem(
    label = label,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    placeholder = placeholder,
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

        MealTypeSearchField(
            modifier = modifier.fillMaxWidth(),
            client = client,
            value = value,
            useDefaultMealTypeIfNull = true,
            label = {
                Row {
                    label()
                    if(!optional) Text("*")
                }
            },

            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            placeholder = placeholder,
            prefix = prefix,
            suffix = suffix,

            isError = error != null || generalError != null,
            supportingText = if(error != null || generalError != null) {
                {
                    Text(
                        text = error ?: generalError ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else null,

            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),

            onValueChange = {
                generalError = null
                onValueChange(it)

                error = if(it == null)
                    null
                else
                    check(it)
            }
        )
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