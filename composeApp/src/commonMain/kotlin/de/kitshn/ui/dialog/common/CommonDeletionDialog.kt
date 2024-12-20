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
import androidx.compose.ui.unit.dp
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_abort
import kitshn.composeapp.generated.resources.action_confirm
import kitshn.composeapp.generated.resources.action_delete
import kitshn.composeapp.generated.resources.common_delete_permanently
import kitshn.composeapp.generated.resources.common_delete_permanently_text
import org.jetbrains.compose.resources.stringResource

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
            Icon(Icons.Rounded.Delete, stringResource(Res.string.action_delete))
        },
        title = {
            Text(stringResource(Res.string.common_delete_permanently))
        },
        text = {
            Text(
                text = stringResource(Res.string.common_delete_permanently_text)
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    state.dismiss()
                    onConfirm(state.model!!)
                }
            ) {
                Text(stringResource(Res.string.action_confirm))
            }
        },
        dismissButton = {
            FilledTonalButton(
                onClick = {
                    state.dismiss()
                    onDismiss()
                }
            ) {
                Text(stringResource(Res.string.action_abort))
            }
        }
    )
}