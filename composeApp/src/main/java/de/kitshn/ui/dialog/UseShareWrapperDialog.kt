package de.kitshn.ui.dialog

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import de.kitshn.KitshnViewModel
import de.kitshn.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun rememberUseShareWrapperDialogState(
    vm: KitshnViewModel
): UseShareWrapperDialogState {
    return remember {
        UseShareWrapperDialogState(vm)
    }
}

class UseShareWrapperDialogState(
    val vm: KitshnViewModel,
    val shown: MutableState<Boolean> = mutableStateOf(false),
    private val callback: MutableState<(decision: Boolean) -> Unit> = mutableStateOf({ })
) {
    private var shareWrapper: String =
        ContextCompat.getString(vm.context, R.string.share_wrapper_url)

    suspend fun open(
        callback: (decision: Boolean) -> Unit
    ) {
        if(vm.settings.getUseShareWrapperHintShown.first() == shareWrapper) {
            callback(vm.settings.getUseShareWrapper.first())
            return
        }

        this.shown.value = true
        this.callback.value = callback
    }

    suspend fun submit(decision: Boolean) {
        vm.settings.setUseShareWrapperHintShown(shareWrapper)
        vm.settings.setUseShareWrapper(decision)

        callback.value(decision)
        dismiss()
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@Composable
fun UseShareWrapperDialog(
    state: UseShareWrapperDialogState
) {
    val coroutineScope = rememberCoroutineScope()
    if(!state.shown.value) return

    AlertDialog(
        modifier = Modifier.padding(16.dp),
        onDismissRequest = {
            state.dismiss()
        },
        icon = {
            Icon(Icons.Rounded.Share, stringResource(R.string.share_wrapper_dialog_title))
        },
        title = {
            Text(stringResource(R.string.share_wrapper_dialog_title))
        },
        text = {
            Text(
                stringResource(
                    id = R.string.share_wrapper_dialog_message,
                    stringResource(id = R.string.share_wrapper_url)
                )
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        state.submit(true)
                    }
                }
            ) {
                Text(stringResource(R.string.action_yes))
            }
        },
        dismissButton = {
            FilledTonalButton(
                onClick = {
                    coroutineScope.launch {
                        state.submit(false)
                    }
                }
            ) {
                Text(stringResource(R.string.action_no))
            }
        }
    )
}