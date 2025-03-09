package de.kitshn.ui.dialog.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Scale
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.kitshn.Platforms
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.platformDetails
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.input.FoodSearchField
import de.kitshn.ui.component.input.NumberField
import de.kitshn.ui.component.input.UnitSearchField
import de.kitshn.ui.dialog.AdaptiveFullscreenDialog
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_add
import kitshn.composeapp.generated.resources.action_add_to_shopping
import kitshn.composeapp.generated.resources.common_amount
import kitshn.composeapp.generated.resources.common_food
import kitshn.composeapp.generated.resources.common_unit
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberShoppingListEntryCreationDialogState(): ShoppingListEntryCreationDialogState {
    return remember {
        ShoppingListEntryCreationDialogState()
    }
}

class ShoppingListEntryCreationDialogState(
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
fun ShoppingListEntryCreationDialog(
    client: TandoorClient,
    state: ShoppingListEntryCreationDialogState,
    onUpdate: (entry: TandoorShoppingListEntry) -> Unit
) {
    if(!state.shown.value) return

    val coroutineScope = rememberCoroutineScope()

    val foodInputFocusRequester = remember { FocusRequester() }
    val amountInputFocusRequester = remember { FocusRequester() }
    val unitInputFocusRequester = remember { FocusRequester() }

    var food by remember { mutableStateOf<String?>(null) }
    var amount by remember { mutableStateOf<Int?>(null) }
    var unit by remember { mutableStateOf<String?>(null) }

    val addRequestState = rememberTandoorRequestState()

    fun create() {
        coroutineScope.launch {
            addRequestState.wrapRequest {
                val entry = client.shopping.add(amount?.toDouble(), food, unit)
                onUpdate(entry)

                state.dismiss()
            }
        }
    }

    AdaptiveFullscreenDialog(
        onDismiss = {
            state.dismiss()
        },
        title = {
            Text(
                text = stringResource(Res.string.action_add_to_shopping)
            )
        },
        actions = {
            Button(
                onClick = {
                    create()
                }
            ) {
                Text(
                    text = stringResource(Res.string.action_add)
                )
            }
        },
        maxWidth = 400.dp
    ) { _, _, _ ->
        Column(
            Modifier.padding(16.dp)
        ) {
            FoodSearchField(
                modifier = Modifier.fillMaxWidth(),
                dropdownMenuModifier = Modifier
                    .focusRequester(foodInputFocusRequester),

                client = client,
                value = food,

                leadingIcon = {
                    Icon(
                        Icons.AutoMirrored.Rounded.Label,
                        stringResource(Res.string.common_food)
                    )
                },
                label = { Text(text = stringResource(Res.string.common_food)) },

                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),

                onValueChange = {
                    food = it
                },

                onSelect = {
                    amountInputFocusRequester.requestFocus()
                }
            )

            Spacer(Modifier.height(16.dp))

            HorizontalDivider()

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NumberField(
                    modifier = Modifier.fillMaxWidth(0.5f)
                        .focusRequester(amountInputFocusRequester),

                    value = amount,

                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Numbers,
                            stringResource(Res.string.common_amount)
                        )
                    },
                    label = { Text(text = stringResource(Res.string.common_amount)) },

                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),

                    onValueChange = {
                        amount = it
                    }
                )

                UnitSearchField(
                    modifier = Modifier.fillMaxWidth()
                        .focusRequester(unitInputFocusRequester),
                    dropdownMenuModifier = Modifier,

                    client = client,
                    value = unit,

                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Scale,
                            stringResource(Res.string.common_unit)
                        )
                    },
                    label = { Text(text = stringResource(Res.string.common_unit)) },

                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            create()
                        }
                    ),

                    onValueChange = {
                        unit = it
                    },

                    onSelect = {
                        unitInputFocusRequester.freeFocus()
                    }
                )
            }
        }

        LaunchedEffect(Unit) {
            if(platformDetails.platform == Platforms.IOS) return@LaunchedEffect
            foodInputFocusRequester.requestFocus()
        }
    }

    TandoorRequestErrorHandler(addRequestState)
}