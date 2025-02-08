package de.kitshn.ui.dialog.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Scale
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarketCategory
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.icons.IconWithState
import de.kitshn.ui.component.input.FoodSearchField
import de.kitshn.ui.component.input.NumberField
import de.kitshn.ui.component.input.UnitSearchField
import de.kitshn.ui.modifier.hideOnAndroid
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_add_to_shopping
import kitshn.composeapp.generated.resources.action_create
import kitshn.composeapp.generated.resources.common_amount
import kitshn.composeapp.generated.resources.common_food
import kitshn.composeapp.generated.resources.common_unit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberShoppingListEntryAddBottomSheetState(): ShoppingListEntryAddBottomSheetState {
    return remember {
        ShoppingListEntryAddBottomSheetState()
    }
}

class ShoppingListEntryAddBottomSheetState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {
    fun open() {
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListEntryAddBottomSheet(
    client: TandoorClient,
    state: ShoppingListEntryAddBottomSheetState,
    onUpdate: (entry: TandoorShoppingListEntry) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val modalBottomSheetState = rememberModalBottomSheetState()

    var render by remember { mutableStateOf(false) }

    LaunchedEffect(
        state.shown.value
    ) {
        if(state.shown.value) {
            render = true
            modalBottomSheetState.show()
        } else {
            modalBottomSheetState.hide()
            render = false
        }
    }

    if(!render) return

    val addRequestState = rememberTandoorRequestState()

    var food by remember { mutableStateOf<String?>(null) }
    var amount by remember { mutableStateOf<Int?>(null) }
    var unit by remember { mutableStateOf<String?>(null) }
    var supermarketCategory by remember { mutableStateOf<TandoorSupermarketCategory?>(null) }

    // workaround to fix dropdown issue
    var isFoodInputFocused by remember { mutableStateOf(false) }

    val foodInputFocusRequester = remember { FocusRequester() }
    val amountInputFocusRequester = remember { FocusRequester() }
    val unitInputFocusRequester = remember { FocusRequester() }

    // set default category for food
    LaunchedEffect(food) {
        val foodModel = client.container.foodByName[food?.lowercase()] ?: return@LaunchedEffect
        supermarketCategory = foodModel.supermarket_category
    }

    fun create() {
        coroutineScope.launch {
            addRequestState.wrapRequest {
                val entry = client.shopping.add(amount?.toDouble(), food, unit)
                onUpdate(entry)

                state.dismiss()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            state.dismiss()
        },
        sheetState = modalBottomSheetState
    ) {
        TopAppBar(
            windowInsets = WindowInsets(0.dp),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            title = {
                Text(
                    text = stringResource(Res.string.action_add_to_shopping)
                )
            },
            actions = {
                FilledIconButton(
                    onClick = {
                        create()
                    }
                ) {
                    IconWithState(
                        progressIndicatorTint = LocalContentColor.current,
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(Res.string.action_create),
                        state = addRequestState.state.toIconWithState()
                    )
                }
            }
        )

        Column(
            Modifier.padding(16.dp)
        ) {
            FoodSearchField(
                modifier = Modifier.fillMaxWidth()
                    .focusRequester(foodInputFocusRequester)
                    .onFocusChanged { isFoodInputFocused = it.isFocused },
                dropdownMenuModifier = Modifier,

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

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.hideOnAndroid(isFoodInputFocused),
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

        // focus food input when opening
        LaunchedEffect(Unit) {
            delay(500)
            foodInputFocusRequester.requestFocus()
        }
    }

    TandoorRequestErrorHandler(addRequestState)
}