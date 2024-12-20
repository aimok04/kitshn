package de.kitshn.ui.dialog.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.kitshn.R

@Composable
fun <T> rememberCommonDeletionDialogState(): CommonDeletionDialogState<T> {
    return remember {
        CommonDeletionDialogState()
    }
}

class CommonDeletionDialogState<T>(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {
    var model: T? = null

    fun open(model: T) {
        this.model = model
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@Composable
fun <T> CommonDeletionDialog(
    state: CommonDeletionDialogState<T>,
    onConfirm: (model: T) -> Unit,
    onDismiss: () -> Unit = { }
) {
    if(!state.shown.value) return

    AlertDialog(
        modifier = Modifier.padding(16.dp),
        onDismissRequest = {
            state.dismiss()
        },
        icon = {
            Icon(Icons.Rounded.Delete, stringResource(R.string.action_delete))
        },
        title = {
            Text(stringResource(R.string.common_delete_permanently))
        },
        text = {
            Text(
                text = stringResource(R.string.common_delete_permanently_text)
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    state.dismiss()
                    onConfirm(state.model!!)
                }
            ) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            FilledTonalButton(
                onClick = {
                    state.dismiss()
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.action_abort))
            }
        }
    )
}