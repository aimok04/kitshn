package de.kitshn.ui.dialog.shopping

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.ui.component.input.DoubleField
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_minus
import kitshn.composeapp.generated.resources.action_plus
import kitshn.composeapp.generated.resources.action_save
import kitshn.composeapp.generated.resources.common_count
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberShoppingListEntryChangeDialogState(): ShoppingListEntryChangeDialogState {
    return remember {
        ShoppingListEntryChangeDialogState()
    }
}

class ShoppingListEntryChangeDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false),
    val entry: MutableState<TandoorShoppingListEntry?> = mutableStateOf(null)
) {
    fun open(entry: TandoorShoppingListEntry) {
        this.entry.value = entry
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShoppingListEntryChangeDialog(
    state: ShoppingListEntryChangeDialogState,
    onChangeAmount: (
        entry: TandoorShoppingListEntry,
        amount: Double?
    ) -> Unit
) {
    if(!state.shown.value) return

    val focusManager = LocalFocusManager.current

    var amount by remember { mutableStateOf(state.entry.value?.amount) }

    fun submit() {
        state.dismiss()
        onChangeAmount(state.entry.value!!, amount)
    }

    AlertDialog(
        modifier = Modifier.padding(16.dp)
            .widthIn(max = 300.dp),
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
            Column {
                DoubleField(
                    modifier = Modifier.fillMaxWidth(),

                    label = {
                        Text(
                            state.entry.value?.unit?.name ?: stringResource(Res.string.common_count)
                        )
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

                    value = amount,
                    onValueChange = {
                        amount = it
                    }
                )

                Spacer(Modifier.height(8.dp))

                ButtonGroup(
                    overflowIndicator = { }
                ) {
                    customItem(
                        buttonGroupContent = {
                            val interactionSource = MutableInteractionSource()

                            Button(
                                onClick = {
                                    focusManager.clearFocus(force = true)
                                    amount = (amount ?: 0.0) - 1
                                },
                                interactionSource = interactionSource,
                                modifier = Modifier
                                    .animateWidth(interactionSource)
                                    .weight(0.5f),
                            ) {
                                Icon(Icons.Rounded.Remove, stringResource(Res.string.action_minus))
                            }
                        },
                        menuContent = { }
                    )

                    customItem(
                        buttonGroupContent = {
                            val interactionSource = MutableInteractionSource()

                            Button(
                                onClick = {
                                    focusManager.clearFocus(force = true)
                                    amount = (amount ?: 0.0) + 1
                                },
                                interactionSource = interactionSource,
                                modifier = Modifier
                                    .animateWidth(interactionSource)
                                    .weight(0.5f),
                            ) {
                                Icon(Icons.Rounded.Add, stringResource(Res.string.action_plus))
                            }
                        },
                        menuContent = { }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                submit()
            }) {
                Text(stringResource(Res.string.action_save))
            }
        }
    )
}