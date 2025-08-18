package de.kitshn.ui.dialog.shopping

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorMealPlan
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.formatAmount
import de.kitshn.handleTandoorRequestState
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.icons.IconWithState
import de.kitshn.ui.component.model.shopping.IndividualShoppingListEntryDetailCard
import de.kitshn.ui.dialog.select.SelectSupermarketCategoryDialog
import de.kitshn.ui.dialog.select.rememberSelectSupermarketCategoryDialogState
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_delete
import kitshn.composeapp.generated.resources.action_mark_as_done
import kitshn.composeapp.generated.resources.common_category
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberShoppingListEntryDetailsBottomSheetState(): ShoppingListEntryDetailsBottomSheetState {
    return remember {
        ShoppingListEntryDetailsBottomSheetState()
    }
}

class ShoppingListEntryDetailsBottomSheetState(
    val shown: MutableState<Boolean> = mutableStateOf(false),
    val linkContent: MutableState<List<TandoorShoppingListEntry>?> = mutableStateOf(null)
) {
    fun open(linkContent: List<TandoorShoppingListEntry>) {
        this.linkContent.value = linkContent
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListEntryDetailsBottomSheet(
    client: TandoorClient,
    showFractionalValues: Boolean,
    state: ShoppingListEntryDetailsBottomSheetState,
    isOffline: Boolean,
    onCheck: (entries: List<TandoorShoppingListEntry>) -> Unit,
    onDelete: (entries: List<TandoorShoppingListEntry>) -> Unit,
    onClickMealplan: (mealplan: TandoorMealPlan) -> Unit,
    onClickRecipe: (recipe: TandoorRecipe) -> Unit,
    onUpdate: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    val modalBottomSheetState = rememberModalBottomSheetState()

    val entries = state.linkContent.value ?: return
    val food = state.linkContent.value!!.first().food

    LaunchedEffect(
        state.shown.value
    ) {
        if(state.shown.value) {
            modalBottomSheetState.show()
        } else {
            modalBottomSheetState.hide()
            state.linkContent.value = null
        }
    }

    val amountChips = remember { mutableStateListOf<Pair<String, Boolean>>() }

    LaunchedEffect(entries) {
        amountChips.clear()
        amountChips.addAll(
            entries.filter { it.amount != 0.0 || it.unit != null }
                .groupBy { it.unit?.id ?: -100 }
                .values
                .map { entryList ->
                    val sharedAmount = entryList.sumOf { it.amount }
                    val sharedUnit = entryList[0].unit

                    Pair(
                        sharedAmount.formatAmount(showFractionalValues) +
                                (sharedUnit?.name?.let { " $it" } ?: ""),
                        entryList.all { it.checked }
                    )
                }
        )
    }

    val requestState = rememberTandoorRequestState()
    val categoryChangeRequestState = rememberTandoorRequestState()

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
                    text = food.name
                )
            },
            actions = {
                IconButton(
                    onClick = {
                        onCheck(entries)
                        state.dismiss()
                    }
                ) {
                    Icon(Icons.Rounded.Check, stringResource(Res.string.action_mark_as_done))
                }

                IconButton(
                    onClick = {
                        onDelete(entries)
                        state.dismiss()
                    }
                ) {
                    Icon(Icons.Rounded.Delete, stringResource(Res.string.action_delete))
                }
            }
        )

        if(amountChips.size > 0) {
            HorizontalDivider()

            Row(
                modifier = Modifier.horizontalScroll(
                    rememberScrollState()
                ).padding(top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(Modifier.width(8.dp))

                amountChips.forEach {
                    ElevatedAssistChip(
                        label = {
                            Text(
                                text = it.first,
                                textDecoration = if(it.second) {
                                    TextDecoration.LineThrough
                                } else {
                                    TextDecoration.None
                                }
                            )
                        },
                        colors = AssistChipDefaults.elevatedAssistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = AssistChipDefaults.elevatedAssistChipElevation(0.dp),
                        onClick = { }
                    )
                }

                Spacer(Modifier.width(8.dp))
            }
        }

        if(!isOffline) {
            HorizontalDivider()

            Box(
                Modifier.padding(16.dp)
            ) {
                val selectSupermarketCategoryDialogState =
                    rememberSelectSupermarketCategoryDialogState()
                var changeSupermarketCategoryValue by remember { mutableStateOf(food.supermarket_category) }

                TextField(
                    modifier = Modifier.fillMaxWidth(),

                    readOnly = true,

                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Category,
                            stringResource(Res.string.common_category)
                        )
                    },
                    label = { Text(text = stringResource(Res.string.common_category)) },
                    value = changeSupermarketCategoryValue?.name ?: "",

                    trailingIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    categoryChangeRequestState.wrapRequest {
                                        changeSupermarketCategoryValue = null
                                        food.updateSupermarketCategory(client, null)

                                        // update state after update
                                        changeSupermarketCategoryValue = food.supermarket_category
                                        onUpdate()
                                    }

                                    hapticFeedback.handleTandoorRequestState(
                                        categoryChangeRequestState
                                    )
                                }
                            }
                        ) {
                            IconWithState(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = stringResource(Res.string.action_delete),
                                state = categoryChangeRequestState.state.toIconWithState()
                            )
                        }
                    },

                    interactionSource = remember { MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect {
                                    if(it !is FocusInteraction.Focus && it !is PressInteraction.Release) return@collect
                                    selectSupermarketCategoryDialogState.open(
                                        changeSupermarketCategoryValue
                                    )
                                }
                            }
                        },

                    onValueChange = { }
                )

                SelectSupermarketCategoryDialog(
                    client = client,
                    state = selectSupermarketCategoryDialogState
                ) {
                    coroutineScope.launch {
                        categoryChangeRequestState.wrapRequest {
                            changeSupermarketCategoryValue = it
                            food.updateSupermarketCategory(client, it)

                            // update state after update
                            changeSupermarketCategoryValue = food.supermarket_category
                            onUpdate()
                        }

                        hapticFeedback.handleTandoorRequestState(categoryChangeRequestState)
                    }
                }
            }
        }

        if(entries.isNotEmpty()) {
            HorizontalDivider()

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                entries.forEach {
                    IndividualShoppingListEntryDetailCard(
                        entry = it,
                        onClick = {
                            coroutineScope.launch {
                                if(it.list_recipe_data?.mealplan != null) {
                                    requestState.wrapRequest {
                                        onClickMealplan(client.mealPlan.get(id = it.list_recipe_data.mealplan))
                                    }
                                } else if(it.list_recipe_data?.recipe != null) {
                                    requestState.wrapRequest {
                                        onClickRecipe(client.recipe.get(id = it.list_recipe_data.recipe))
                                    }
                                }
                            }
                        },
                        onClickCheck = {
                            onCheck(listOf(it))
                        }
                    )
                }
            }
        }
    }

    TandoorRequestErrorHandler(categoryChangeRequestState)

    TandoorRequestErrorHandler(requestState)
}