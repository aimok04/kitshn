package de.kitshn.ui.dialog.servings

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.kitshn.ui.component.input.DoubleField
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_apply
import kitshn.composeapp.generated.resources.common_count
import kitshn.composeapp.generated.resources.common_portions
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberServingsChangeDialogState(): ServingsChangeDialogState {
    return remember {
        ServingsChangeDialogState()
    }
}

class ServingsChangeDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {

    var servings by mutableDoubleStateOf(1.0)

    fun open(servings: Double) {
        this.servings = servings
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@Composable
fun ServingsChangeDialog(
    portionText: String = stringResource(Res.string.common_portions),
    state: ServingsChangeDialogState,
    onSubmit: (servings: Double) -> Unit
) {
    if (!state.shown.value) return

    val submit = {
        onSubmit(state.servings)
        state.dismiss()
    }

    AlertDialog(
        modifier = Modifier.padding(16.dp),
        onDismissRequest = {
            state.dismiss()
        },
        icon = {
            Icon(Icons.Rounded.Numbers, stringResource(Res.string.common_count))
        },
        title = {
            Text(stringResource(Res.string.common_count))
        },
        text = {
            val focusRequester = remember { FocusRequester() }

            DoubleField(
                modifier = Modifier.focusRequester(focusRequester),
                label = {
                    Text(portionText)
                },
                min = 0.1,

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        submit()
                    }
                ),

                value = state.servings,
                onValueChange = {
                    state.servings = it ?: 1.0
                }
            )

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        },
        confirmButton = {
            Button(onClick = {
                submit()
            }) {
                Text(stringResource(Res.string.action_apply))
            }
        }
    )
}