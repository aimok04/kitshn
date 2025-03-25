package de.kitshn.model.form.item.field

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.route.TandoorUser
import de.kitshn.ui.component.input.SelectUsersField
import kotlinx.coroutines.launch

class KitshnFormSelectUsersFieldItem(
    val client: TandoorClient,
    val value: () -> List<TandoorUser>,
    val onValueChange: (value: List<TandoorUser>) -> Unit,

    val dialogTitle: @Composable () -> String,
    val dialogEmptyErrorText: @Composable () -> String,

    label: @Composable () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,

    optional: Boolean = false,

    val check: suspend (data: List<TandoorUser>) -> String?
) : KitshnFormBaseFieldItem(
    label = label,
    leadingIcon = leadingIcon,
    optional = optional
) {

    @Composable
    override fun Render() {
        var error by rememberSaveable { mutableStateOf<String?>(null) }
        val value = value()

        val coroutineScope = rememberCoroutineScope()

        SelectUsersField(
            modifier = Modifier.fillMaxWidth(),
            client = client,
            value = value,
            label = {
                Row {
                    label()
                    if(!optional) Text("*")
                }
            },

            leadingIcon = leadingIcon,

            dialogTitle = dialogTitle(),
            dialogEmptyErrorText = dialogEmptyErrorText(),

            onValueChange = {
                coroutineScope.launch {
                    generalError = null
                    onValueChange(it)

                    error = check(it)
                }
            }
        )
    }

    override suspend fun submit(): Boolean {
        val value = value()
        val checkResult = check(value)

        if(checkResult != null) {
            generalError = checkResult
            return false
        } else {
            generalError = null
            return true
        }
    }

}