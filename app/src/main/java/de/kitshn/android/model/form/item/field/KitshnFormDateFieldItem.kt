package de.kitshn.android.model.form.item.field

import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import de.kitshn.android.R
import de.kitshn.android.ui.component.input.DateField
import java.time.LocalDate

class KitshnFormDateFieldItem(
    val value: () -> LocalDate?,
    val onValueChange: (value: LocalDate?) -> Unit,

    label: @Composable () -> Unit,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,

    val minDate: @Composable () -> LocalDate? = { null },
    val maxDate: @Composable () -> LocalDate? = { null },

    optional: Boolean = false,

    val check: (value: LocalDate?) -> String?
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
        var error by rememberSaveable { mutableStateOf<String?>(null) }
        val value = value()

        context = LocalContext.current

        DateField(
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

            minDate = minDate(),
            maxDate = maxDate(),

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
                generalError = null
                onValueChange(it)

                error = if(it == null)
                    null
                else
                    check(it)
            }
        )
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