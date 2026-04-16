package de.kitshn.model.form.item.field

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.kitshn.ui.component.input.TimeField
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.form_error_field_empty
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.getString

class KitshnFormTimeFieldItem(
    val value: () -> LocalTime?,
    val onValueChange: (value: LocalTime?) -> Unit,

    label: @Composable () -> Unit,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,

    optional: Boolean = false,

    val check: (value: LocalTime?) -> String?
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
        var error by rememberSaveable { mutableStateOf<String?>(null) }
        val value = value()

        TimeField(
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
