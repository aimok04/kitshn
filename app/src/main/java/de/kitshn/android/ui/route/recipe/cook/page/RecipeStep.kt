package de.kitshn.android.ui.route.recipe.cook.page

import android.content.Intent
import android.provider.AlarmClock
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.kitshn.android.KitshnViewModel
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.model.TandoorStep
import de.kitshn.android.ui.component.model.ingredient.IngredientsList
import de.kitshn.android.ui.component.model.recipe.step.RecipeStepRecipeLink
import de.kitshn.android.ui.dialog.recipe.RecipeLinkBottomSheet
import de.kitshn.android.ui.dialog.recipe.rememberRecipeLinkBottomSheetState
import de.kitshn.android.ui.layout.ResponsiveSideBySideLayout
import de.kitshn.android.ui.view.ViewParameters
import kotlin.math.roundToInt

@Composable
fun RouteRecipeCookPageStep(
    vm: KitshnViewModel,
    step: TandoorStep,
    servingsFactor: Double
) {
    val context = LocalContext.current

    @Composable
    fun InstructionText(
        maxHeightPx: Int,
        sideBySideLayout: Boolean
    ) {
        BoxWithConstraints(
            Modifier.fillMaxSize()
        ) {
            val density = LocalDensity.current

            val textMeasurer = rememberTextMeasurer()
            var fontSize by remember { mutableStateOf(14.sp) }

            val maxWidthPx = with(density) { maxWidth.roundToPx() }
            LaunchedEffect(step.instruction, sideBySideLayout) {
                var newFontSize = 14

                while(newFontSize < 44) {
                    val textLayout = textMeasurer.measure(
                        text = step.instruction,
                        style = TextStyle(
                            fontSize = newFontSize.sp,
                            lineHeight = newFontSize.sp
                        ),
                        constraints = Constraints(
                            maxWidth = maxWidthPx
                        )
                    )

                    if(textLayout.size.height > maxHeightPx) break
                    newFontSize += 2
                }

                fontSize = newFontSize.sp
            }

            Text(
                modifier = Modifier.padding(16.dp),
                text = step.instruction,
                fontSize = fontSize,
                lineHeight = fontSize
            )
        }
    }

    val verticalScroll = rememberScrollState()
    val recipeLinkBottomSheetState = rememberRecipeLinkBottomSheetState()

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
    ) {
        val density = LocalDensity.current
        val maxHeightPx = with(density) { maxWidth.roundToPx() }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(verticalScroll)
        ) {
            if(step.time > 0) AssistChip(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                onClick = {
                    context.startActivity(
                        Intent().apply {
                            action = AlarmClock.ACTION_SET_TIMER
                            putExtra(AlarmClock.EXTRA_LENGTH, step.time * 60)
                            putExtra(AlarmClock.EXTRA_MESSAGE, step.name)
                            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                        }
                    )

                    Toast.makeText(
                        context,
                        context.getString(R.string.recipe_step_timer_created), Toast.LENGTH_SHORT
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

            if(step.ingredients.isEmpty()) {
                if(step.step_recipe != null) RecipeStepRecipeLink(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    step = step
                ) {
                    recipeLinkBottomSheetState.open(it.toOverview())
                }

                InstructionText(maxHeightPx, false)
            } else {
                ResponsiveSideBySideLayout(
                    rightMinWidth = 300.dp,
                    rightMaxWidth = 300.dp,
                    leftMinWidth = 300.dp,
                    leftLayout = {
                        InstructionText(
                            if(it) (maxHeightPx / 2.5f).roundToInt() else maxHeightPx,
                            it
                        )
                    }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        ) {
                            RecipeStepRecipeLink(
                                modifier = Modifier.fillMaxWidth(),
                                step = step
                            ) {
                                recipeLinkBottomSheetState.open(it.toOverview())
                            }
                        }

                        Card(
                            Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        ) {
                            Box {
                                IngredientsList(
                                    list = step.ingredients,
                                    factor = servingsFactor,
                                    colors = ListItemDefaults.colors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    RecipeLinkBottomSheet(
        p = ViewParameters(
            vm, null
        ),
        state = recipeLinkBottomSheetState
    )
}