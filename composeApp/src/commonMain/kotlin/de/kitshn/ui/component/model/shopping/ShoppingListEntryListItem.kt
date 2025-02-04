package de.kitshn.ui.component.model.shopping

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.model.TandoorFood
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntryRecipeMealplan
import de.kitshn.formatAmount
import de.kitshn.ui.modifier.loadingPlaceHolder
import de.kitshn.ui.selectionMode.SelectionModeState
import de.kitshn.ui.selectionMode.values.selectionModeListItemColors
import de.kitshn.ui.state.ErrorLoadingSuccessState
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.lorem_ipsum_short
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShoppingListEntryListItemPlaceholder(
    modifier: Modifier = Modifier,
    loadingState: ErrorLoadingSuccessState = ErrorLoadingSuccessState.LOADING
) {
    ListItem(
        modifier = modifier.fillMaxWidth(),
        colors = ListItemDefaults.colors(
            supportingColor = MaterialTheme.colorScheme.primary
        ),
        headlineContent = {
            Text(
                text = stringResource(Res.string.lorem_ipsum_short) + stringResource(Res.string.lorem_ipsum_short),
                modifier = Modifier.loadingPlaceHolder(loadingState)
            )
        },
        supportingContent = {
            Row(
                modifier = Modifier.horizontalScroll(
                    rememberScrollState()
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ElevatedAssistChip(
                    label = {
                        Text(
                            text = stringResource(Res.string.lorem_ipsum_short).substring(5),
                            modifier = Modifier.loadingPlaceHolder(loadingState)
                        )
                    },
                    colors = AssistChipDefaults.elevatedAssistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = AssistChipDefaults.elevatedAssistChipElevation(0.dp),
                    onClick = { }
                )
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShoppingListEntryListItem(
    modifier: Modifier = Modifier,

    food: TandoorFood,
    entries: List<TandoorShoppingListEntry>,

    selectionState: SelectionModeState<Int>,

    showFractionalValues: Boolean,

    onClick: () -> Unit
) {
    val amountChips = remember { mutableStateListOf<Pair<String, Boolean>>() }
    val mealplans =
        remember { mutableStateListOf<TandoorShoppingListEntryRecipeMealplan>() }

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

        mealplans.clear()
        mealplans.addAll(
            entries.filter { it.recipe_mealplan != null }
                .map { it.recipe_mealplan!! }
        )
    }

    val allChecked = entries.all { it.checked }

    val hapticFeedback = LocalHapticFeedback.current

    val colors = ListItemDefaults.selectionModeListItemColors(
        defaultColors = ListItemDefaults.colors(
            supportingColor = MaterialTheme.colorScheme.primary
        ),
        selected = selectionState.selectedItems.contains(food.id),
    )

    ListItem(
        modifier = modifier.fillMaxWidth()
            .alpha(if(allChecked) 0.7f else 1f)
            .combinedClickable(
                onClick = {
                    if(selectionState.isSelectionModeEnabled()) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectionState.selectToggle(food.id)
                    } else {
                        onClick()
                    }
                },
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectionState.selectToggle(food.id)
                }
            ),
        colors = colors,
        headlineContent = {
            Text(
                text = food.name,
                textDecoration = if(allChecked) {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                }
            )
        },
        supportingContent = if(amountChips.size > 0) {
            {
                Row(
                    modifier = Modifier.horizontalScroll(
                        rememberScrollState()
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                }
            }
        } else {
            null
        }
    )
}