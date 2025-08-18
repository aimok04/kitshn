package de.kitshn.ui.component.model.shopping

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.kitshn.api.tandoor.model.TandoorFood
import de.kitshn.api.tandoor.model.TandoorMealPlan
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.formatAmount
import de.kitshn.ui.modifier.loadingPlaceHolder
import de.kitshn.ui.selectionMode.SelectionModeState
import de.kitshn.ui.selectionMode.values.selectionModeListItemColors
import de.kitshn.ui.state.ErrorLoadingSuccessState
import de.kitshn.ui.theme.Typography
import de.kitshn.ui.theme.playfairDisplay
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_more
import kitshn.composeapp.generated.resources.lorem_ipsum_short
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShoppingListEntryListItemPlaceholder(
    modifier: Modifier = Modifier,
    loadingState: ErrorLoadingSuccessState = ErrorLoadingSuccessState.LOADING,
    enlarge: Boolean = false
) {
    ListItem(
        modifier = modifier.fillMaxWidth(),
        colors = ListItemDefaults.colors(
            supportingColor = MaterialTheme.colorScheme.primary
        ),
        headlineContent = {
            if (enlarge) {
                Text(
                    text = stringResource(Res.string.lorem_ipsum_short) + stringResource(Res.string.lorem_ipsum_short),
                    modifier = Modifier.loadingPlaceHolder(loadingState),
                    style = Typography().headlineMedium,
                    fontFamily = playfairDisplay()
                )
            } else {
                Text(
                    text = stringResource(Res.string.lorem_ipsum_short) + stringResource(Res.string.lorem_ipsum_short),
                    modifier = Modifier.loadingPlaceHolder(loadingState)
                )
            }
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
                        if (enlarge) {
                            Text(
                                text = stringResource(Res.string.lorem_ipsum_short).substring(5),
                                modifier = Modifier.loadingPlaceHolder(loadingState),
                                fontSize = 20.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = stringResource(Res.string.lorem_ipsum_short).substring(5),
                                modifier = Modifier.loadingPlaceHolder(loadingState)
                            )
                        }
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShoppingListEntryListItem(
    modifier: Modifier = Modifier,

    food: TandoorFood,
    entries: List<TandoorShoppingListEntry>,

    selectionState: SelectionModeState<Int>? = null,

    showFractionalValues: Boolean,

    enlarge: Boolean = false,

    onClick: (() -> Unit)? = null,
    onClickExpand: (() -> Unit)? = null
) {
    val amountChips = remember { mutableStateListOf<Pair<String, Boolean>>() }
    val mealplans =
        remember { mutableStateListOf<TandoorMealPlan>() }

    LaunchedEffect(entries) {
        amountChips.clear()
        amountChips.addAll(
            entries.filter { it.amount != 0.0 || it.unit != null }
                .groupBy { it.unit?.id ?: -100 }
                .values
                .map { entryList ->
                    val allItemsChecked = entryList.all { it.checked }

                    val sharedAmount =
                        entryList.filter { (!it.checked || allItemsChecked) }.sumOf { it.amount }
                    val sharedUnit = entryList[0].unit

                    val value = StringBuilder()
                    value.append(sharedAmount.formatAmount(showFractionalValues))
                    if((sharedUnit?.name ?: "").isNotBlank()) value.append(" ${sharedUnit!!.name}")

                    Pair(
                        value.toString(),
                        allItemsChecked
                    )
                }
        )

        mealplans.clear()
        mealplans.addAll(
            entries.filter { it.list_recipe_data?.meal_plan_data != null }
                .map { it.list_recipe_data!!.meal_plan_data!! }
        )
    }

    val allChecked = entries.all { it.checked }

    val hapticFeedback = LocalHapticFeedback.current

    val colors = ListItemDefaults.selectionModeListItemColors(
        defaultColors = ListItemDefaults.colors(
            supportingColor = MaterialTheme.colorScheme.primary
        ),
        selected = selectionState?.selectedItems?.contains(food.id) ?: false,
    )

    ListItem(
        modifier = modifier.fillMaxWidth()
            .alpha(if(allChecked) 0.7f else 1f).run {
                if(onClick != null) {
                    combinedClickable(
                        onClick = {
                            if(selectionState?.isSelectionModeEnabled() == true) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectionState.selectToggle(food.id)
                            } else {
                                onClick()
                            }
                        },
                        onLongClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectionState?.selectToggle(food.id)
                        }
                    )
                } else {
                    this
                }
            },
        colors = colors,
        leadingContent = if(entries.size > 1 && onClickExpand != null) {
            {
                FilledTonalIconButton(
                    onClick = onClickExpand,
                    modifier =
                        Modifier.minimumInteractiveComponentSize()
                            .size(
                                IconButtonDefaults.smallContainerSize(
                                    IconButtonDefaults.IconButtonWidthOption.Narrow
                                )
                            )
                ) {
                    Icon(
                        Icons.Rounded.ExpandMore,
                        contentDescription = stringResource(Res.string.action_more),
                        modifier = Modifier.size(IconButtonDefaults.smallIconSize)
                    )
                }
            }
        } else {
            null
        },
        headlineContent = {
            if (enlarge) {
                Text(
                    text = food.name,
                    style = Typography().headlineMedium,
                    fontFamily = playfairDisplay(),
                    textDecoration = if(allChecked) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    }
                )
            } else {
                Text(
                    text = food.name,
                    textDecoration = if(allChecked) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    }
                )
            }
        },
        supportingContent = {
            Row(
                modifier = Modifier.horizontalScroll(
                    rememberScrollState()
                ).height(48.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                amountChips.forEach {
                    Box(
                        Modifier
                            .padding(top = 8.dp, bottom = 8.dp)
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                            .padding(start = 16.dp, end = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (enlarge) {
                            Text(
                                text = it.first,
                                textDecoration = if(it.second) {
                                    TextDecoration.LineThrough
                                } else {
                                    TextDecoration.None
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 20.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        } else {
                            Text(
                                text = it.first,
                                textDecoration = if(it.second) {
                                    TextDecoration.LineThrough
                                } else {
                                    TextDecoration.None
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    )
}