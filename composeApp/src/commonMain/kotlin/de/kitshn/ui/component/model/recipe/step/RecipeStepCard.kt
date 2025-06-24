package de.kitshn.ui.component.model.recipe.step

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
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.model.TandoorStep
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.formatDuration
import de.kitshn.isLaunchTimerHandlerImplemented
import de.kitshn.launchTimerHandler
import de.kitshn.ui.component.MarkdownRichTextWithTimerDetection
import de.kitshn.ui.component.model.ingredient.IngredientsList
import de.kitshn.ui.layout.ResponsiveSideBySideLayout
import de.kitshn.ui.modifier.loadingPlaceHolder
import de.kitshn.ui.state.ErrorLoadingSuccessState
import de.kitshn.ui.theme.Typography
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_minute_min
import kitshn.composeapp.generated.resources.common_step
import kitshn.composeapp.generated.resources.lorem_ipsum_medium
import org.jetbrains.compose.resources.stringResource

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
    var showIngredientsTable by remember { mutableStateOf(false) }
    LaunchedEffect(step, hideIngredients) {
        if(step == null) return@LaunchedEffect
        showIngredientsTable = (step.ingredients.isNotEmpty()) &&
                (step.show_ingredients_table) &&
                (!hideIngredients)
    }

    val launchTimerHandler = launchTimerHandler()

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
                    Res.string.common_step,
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
                    style = Typography().titleLarge
                )

                Row {
                    if(step != null && step.time > 0) AssistChip(
                        modifier = Modifier.padding(top = 8.dp, end = 16.dp),
                        onClick = {
                            if(isLaunchTimerHandlerImplemented)
                                launchTimerHandler(step.time * 60, stepName)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Timer,
                                stringResource(Res.string.common_minute_min)
                            )
                        },
                        label = { Text(step.time.formatDuration()) }
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
                    ?: stringResource(Res.string.lorem_ipsum_medium)
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
                    Box(
                        Modifier.padding(8.dp)
                    ) {
                        IngredientsList(
                            list = step.ingredients,
                            factor = servingsFactor,
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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