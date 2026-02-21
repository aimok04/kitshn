package de.kitshn.ui.dialog.shopping

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import co.touchlab.kermit.Logger
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.handleTandoorRequestState
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
import kotlinx.coroutines.job
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShoppingListEntryCreationDialog(
    client: TandoorClient,
    state: ShoppingListEntryCreationDialogState,
    onUpdate: (entry: TandoorShoppingListEntry) -> Unit
) {
    if(!state.shown.value) return

    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    val foodInputFocusRequester = remember { FocusRequester() }
    val amountInputFocusRequester = remember { FocusRequester() }
    val unitInputFocusRequester = remember { FocusRequester() }

    var food by remember { mutableStateOf<String?>(null) }
    var amount by remember { mutableStateOf<Int?>(null) }
    var unit by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }

    val addRequestState = rememberTandoorRequestState()

    fun create() {
        coroutineScope.launch {
            isLoading = true

            addRequestState.wrapRequest {
                val entry = client.shopping.add(amount?.toDouble(), food, unit)
                onUpdate(entry)

                state.dismiss()
            }

            isLoading = false

            hapticFeedback.handleTandoorRequestState(addRequestState)
        }
    }

    val twoColumnLayout = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT

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
            AnimatedContent(
                targetState = isLoading,
                transitionSpec = {
                    fadeIn()
                        .togetherWith(fadeOut())
                }
            ) {
                when(it) {
                    true -> {
                        ContainedLoadingIndicator()
                    }

                    false -> Button(
                        onClick = {
                            create()
                        }
                    ) {
                        Text(
                            text = stringResource(Res.string.action_add)
                        )
                    }
                }
            }
        },
        maxWidth = if(twoColumnLayout) 400.dp else 900.dp
    ) { _, _, _ ->
        Column(
            Modifier.padding(16.dp)
        ) {
            @Composable
            fun Food(
                fraction: Float
            ) {
                FoodSearchField(
                    modifier = Modifier.fillMaxWidth(fraction),
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
            }

            @Composable
            fun Amount(
                fraction: Float
            ) {
                NumberField(
                    value = amount,
                    modifier = Modifier.fillMaxWidth(fraction)
                        .focusRequester(amountInputFocusRequester),

                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Numbers,
                            stringResource(Res.string.common_amount)
                        )
                    },
                    label = { Text(text = stringResource(Res.string.common_amount)) },

                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),

                    onValueChange = {
                        amount = it
                    }
                )
            }

            @Composable
            fun Unit(
                fraction: Float
            ) {
                UnitSearchField(
                    modifier = Modifier.fillMaxWidth(fraction)
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

            if(twoColumnLayout) {
                Food(1f)

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Amount(0.5f)
                    Unit(1f)
                }
            }else{
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Food(0.3f)
                    Amount(0.5f)
                    Unit(1f)
                }
            }
        }

        LaunchedEffect(Unit) {
            this.coroutineContext.job.invokeOnCompletion {
                try {
                    foodInputFocusRequester.requestFocus()
                } catch(e: Exception) {
                    Logger.e("ShoppingListEntryCreationDialog.kt", e)
                }
            }
        }
    }

    TandoorRequestErrorHandler(addRequestState)
}