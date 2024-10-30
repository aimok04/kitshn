package de.kitshn.android.ui.component.model.recipe.step

import android.content.Intent
import android.provider.AlarmClock
import android.widget.Toast
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.model.TandoorStep
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.android.ui.component.MarkdownRichTextWithTimerDetection
import de.kitshn.android.ui.component.model.ingredient.IngredientsList
import de.kitshn.android.ui.layout.ResponsiveSideBySideLayout
import de.kitshn.android.ui.modifier.loadingPlaceHolder
import de.kitshn.android.ui.state.ErrorLoadingSuccessState
import de.kitshn.android.ui.theme.Typography

@Composable
fun RecipeStepCard(
    modifier: Modifier = Modifier,
    columnModifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
    colors: CardColors = CardDefaults.cardColors(),
    recipe: TandoorRecipe? = null,
    step: TandoorStep? = null,
    stepIndex: Int = 0,
    hideIngredients: Boolean = false,
    servingsFactor: Double,
    loadingState: ErrorLoadingSuccessState = ErrorLoadingSuccessState.SUCCESS,
    appendAction: @Composable () -> Unit = {},
    showFractionalValues: Boolean,
    onClickRecipeLink: (recipe: TandoorRecipe) -> Unit
) {
    val context = LocalContext.current

    var showIngredientsTable by remember { mutableStateOf(false) }
    LaunchedEffect(step, hideIngredients) {
        if(step == null) return@LaunchedEffect
        showIngredientsTable = (step.ingredients.isNotEmpty()) &&
                (step.show_ingredients_table) &&
                (!hideIngredients)
    }

    Card(
        modifier = modifier,
        interactionSource = interactionSource,
        colors = colors,
        onClick = { }
    ) {
        @Composable
        fun Instructions() {
            val stepName = (step?.name ?: "").ifBlank {
                stringResource(
                    R.string.common_step,
                    stepIndex + 1
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                        .loadingPlaceHolder(loadingState),
                    text = stepName,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = Typography.titleLarge
                )

                Row {
                    if(step != null && step.time > 0) AssistChip(
                        modifier = Modifier.padding(top = 8.dp, end = 16.dp),
                        onClick = {
                            context.startActivity(
                                Intent().apply {
                                    action = AlarmClock.ACTION_SET_TIMER
                                    putExtra(AlarmClock.EXTRA_LENGTH, step.time * 60)
                                    putExtra(AlarmClock.EXTRA_MESSAGE, stepName)
                                    putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                                }
                            )

                            Toast.makeText(
                                context,
                                context.getString(R.string.recipe_step_timer_created),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Timer,
                                stringResource(id = R.string.common_minute_min)
                            )
                        },
                        label = { Text("${step.time} ${stringResource(id = R.string.common_minute_min)}") }
                    )

                    appendAction()
                }
            }

            MarkdownRichTextWithTimerDetection(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .loadingPlaceHolder(loadingState),
                timerName = stepName,
                markdown = step?.instructionsWithTemplating(servingsFactor, showFractionalValues)
                    ?: stringResource(id = R.string.lorem_ipsum_medium)
            )
        }

        Column(
            columnModifier
        ) {
            if(recipe != null && step != null) RecipeStepMultimediaBox(
                recipe = recipe,
                step = step
            )

            if(showIngredientsTable && step != null) {
                var disableSideBySideLayout by remember { mutableStateOf(false) }

                ResponsiveSideBySideLayout(
                    rightMinWidth = 300.dp,
                    rightMaxWidth = 500.dp,
                    leftMinWidth = 300.dp,
                    disable = disableSideBySideLayout,
                    leftLayout = {
                        Instructions()
                    }
                ) {
                    if(!it) HorizontalDivider()

                    Box {
                        IngredientsList(
                            list = step.ingredients,
                            factor = servingsFactor,
                            colors = ListItemDefaults.colors(
                                containerColor = colors.containerColor
                            ),
                            onNotEnoughSpace = {
                                disableSideBySideLayout = true
                            },
                            showFractionalValues = showFractionalValues
                        )
                    }
                }
            } else {
                Instructions()
            }

            if(step?.step_recipe != null) {
                RecipeStepRecipeLink(
                    modifier = Modifier.padding(16.dp),
                    step = step,
                    onClick = onClickRecipeLink
                )
            }
        }
    }
}