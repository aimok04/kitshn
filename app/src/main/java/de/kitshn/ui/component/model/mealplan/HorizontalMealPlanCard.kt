package de.kitshn.ui.component.model.mealplan

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import de.kitshn.R
import de.kitshn.api.tandoor.model.TandoorMealPlan
import de.kitshn.ui.component.model.recipe.HorizontalRecipeCard
import de.kitshn.ui.modifier.loadingPlaceHolder
import de.kitshn.ui.selectionMode.SelectionModeState
import de.kitshn.ui.selectionMode.values.selectionModeCardColors
import de.kitshn.ui.state.ErrorLoadingSuccessState
import de.kitshn.ui.theme.Typography
import de.kitshn.ui.theme.playfairDisplay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalMealPlanCard(
    mealPlan: TandoorMealPlan? = null,
    loadingState: ErrorLoadingSuccessState = ErrorLoadingSuccessState.SUCCESS,
    selectionState: SelectionModeState<Int>? = null,
    onClick: () -> Unit = {}
) {
    val hapticFeedback = LocalHapticFeedback.current

    val colors = CardDefaults.selectionModeCardColors(
        selected = selectionState?.selectedItems?.contains(mealPlan?.id) ?: false,
        defaultCardColors = CardDefaults.elevatedCardColors()
    )

    val mOnLongClick: () -> Unit = {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        mealPlan?.let { selectionState?.selectToggle(it.id) }
    }

    val mOnClick: () -> Unit = {
        if(selectionState?.isSelectionModeEnabled() == true) {
            mOnLongClick()
        } else {
            onClick()
        }
    }

    if(mealPlan?.recipe == null) {
        Card(
            onClick = { }
        ) {
            Column(
                Modifier.combinedClickable(onClick = mOnClick, onLongClick = mOnLongClick)
            ) {
                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = colors.containerColor,
                        headlineColor = colors.contentColor
                    ),
                    headlineContent = {
                        Text(
                            modifier = Modifier.loadingPlaceHolder(loadingState),
                            text = mealPlan?.title
                                ?: stringResource(id = R.string.lorem_ipsum_title),
                            style = Typography.bodyLarge.copy(
                                fontFamily = playfairDisplay
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    supportingContent = {
                        FilterChip(
                            modifier = Modifier.loadingPlaceHolder(loadingState),
                            onClick = { },
                            label = {
                                Text(
                                    text = mealPlan?.meal_type_name
                                        ?: stringResource(id = R.string.lorem_ipsum_short)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedLabelColor = mealPlan?.meal_type?.color ?: Color.Gray,
                                selectedContainerColor = (mealPlan?.meal_type?.color
                                    ?: Color.Gray).copy(alpha = 0.2f)
                            ),
                            selected = true
                        )
                    }
                )
            }
        }
    } else {
        HorizontalRecipeCard(
            colors = colors,
            recipeOverview = mealPlan.recipe,
            supportingContent = {
                FilterChip(
                    onClick = { },
                    label = { Text(text = mealPlan.meal_type_name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedLabelColor = mealPlan.meal_type.color,
                        selectedContainerColor = mealPlan.meal_type.color.copy(alpha = 0.2f)
                    ),
                    selected = true
                )
            },
            onClick = { mOnClick() },
            onLongClick = { mOnLongClick() }
        )
    }
}