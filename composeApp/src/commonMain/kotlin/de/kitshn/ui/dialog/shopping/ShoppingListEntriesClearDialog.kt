package de.kitshn.ui.dialog.shopping

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.kitshn.ui.component.settings.SettingsSwitchListItem
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_abort
import kitshn.composeapp.generated.resources.action_clear
import kitshn.composeapp.generated.resources.action_confirm
import kitshn.composeapp.generated.resources.shopping_list_dialog_clear_only_done
import kitshn.composeapp.generated.resources.shopping_list_dialog_clear_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberShoppingListEntriesClearDialogState(): ShoppingListEntriesClearDialogState {
    return remember {
        ShoppingListEntriesClearDialogState()
    }
}

class ShoppingListEntriesClearDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {
    fun open() {
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@Composable
fun ShoppingListEntriesClearDialog(
    state: ShoppingListEntriesClearDialogState,
    onClear: (onlyDoneEntries: Boolean) -> Unit
) {
    if (!state.shown.value) return

    var onlyDoneEntries by remember { mutableStateOf(true) }

    AlertDialog(
        modifier = Modifier.padding(16.dp),
        onDismissRequest = {
            state.dismiss()
        },
        icon = {
            Icon(Icons.Rounded.CleaningServices, stringResource(Res.string.action_clear))
        },
        title = {
            Text(stringResource(Res.string.shopping_list_dialog_clear_title))
        },
        text = {
            SettingsSwitchListItem(
                label = { Text(stringResource(Res.string.shopping_list_dialog_clear_only_done)) },
                contentDescription = stringResource(Res.string.shopping_list_dialog_clear_only_done),
                checked = onlyDoneEntries,
                onCheckedChanged = {
                    onlyDoneEntries = it
                }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    state.dismiss()
                    onClear(onlyDoneEntries)
                }
            ) {
                Text(stringResource(Res.string.action_confirm))
            }
        },
        dismissButton = {
            FilledTonalButton(
                onClick = {
                    state.dismiss()
                }
            ) {
                Text(stringResource(Res.string.action_abort))
            }
        }
    )
}