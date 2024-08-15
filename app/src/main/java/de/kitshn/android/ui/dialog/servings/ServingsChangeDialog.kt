package de.kitshn.android.ui.dialog.servings

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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.kitshn.android.R
import de.kitshn.android.ui.component.input.NumberField

@Composable
fun rememberServingsChangeDialogState(): ServingsChangeDialogState {
    return remember {
        ServingsChangeDialogState()
    }
}

class ServingsChangeDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {

    var servings by mutableIntStateOf(1)

    fun open(servings: Int) {
        this.servings = servings
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@Composable
fun ServingsChangeDialog(
    portionText: String = stringResource(id = R.string.common_portions),
    state: ServingsChangeDialogState,
    onSubmit: (servings: Int) -> Unit
) {
    if(!state.shown.value) return

    val focusRequester = remember { FocusRequester() }

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
            Icon(Icons.Rounded.Numbers, stringResource(R.string.common_count))
        },
        title = {
            Text(stringResource(R.string.common_count))
        },
        text = {
            NumberField(
                modifier = Modifier.focusRequester(focusRequester),
                label = {
                    Text(portionText)
                },
                min = 1,

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
                    state.servings = it ?: 1
                }
            )
        },
        confirmButton = {
            Button(onClick = {
                submit()
            }) {
                Text(stringResource(R.string.action_apply))
            }
        }
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}