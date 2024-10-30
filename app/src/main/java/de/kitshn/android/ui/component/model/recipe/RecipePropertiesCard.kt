package de.kitshn.android.ui.component.model.recipe

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.model.TandoorFoodProperty
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipeProperty
import de.kitshn.android.formatAmount
import de.kitshn.android.ui.theme.Typography

@Composable
fun RecipePropertiesCard(
    modifier: Modifier = Modifier,
    columnModifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
    colors: CardColors = CardDefaults.cardColors(),
    recipe: TandoorRecipe? = null,
    servingsFactor: Double? = 1.0,
    showFractionalValues: Boolean,
    prependContent: @Composable () -> Unit = { }
) {
    if(recipe == null) return

    val foodProperties = remember { mutableStateListOf<TandoorFoodProperty>() }
    val recipeProperties = remember { mutableStateListOf<TandoorRecipeProperty>() }

    LaunchedEffect(recipe) {
        foodProperties.clear()
        foodProperties.addAll(recipe.getRelevantFoodProperties())
        foodProperties.sortBy { it.order }

        recipeProperties.clear()
        recipeProperties.addAll(recipe.getRelevantRecipeProperties())
        recipeProperties.sortBy { it.property_type.order }
    }

    if(foodProperties.size == 0 && recipeProperties.size == 0) return

    var showFoodProperties by rememberSaveable { mutableStateOf(true) }

    prependContent()

    Card(
        modifier = modifier,
        interactionSource = interactionSource,
        colors = colors,
        onClick = { }
    ) {
        Column(
            columnModifier
        ) {
            Text(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                text = stringResource(R.string.common_properties),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = Typography.titleLarge
            )

            if(foodProperties.size > 0 && recipeProperties.size > 0) {
                Box(
                    Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    RecipePropertiesCardFoodRecipeToggle(
                        foodActive = showFoodProperties
                    ) {
                        showFoodProperties = it
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            @Composable
            fun TableTextBox(
                text: String,
                bold: Boolean = false,
                weight: Float,
                contentAlignment: Alignment = Alignment.CenterEnd
            ) {
                Box(
                    Modifier
                        .weight(weight)
                        .padding(
                            top = 4.dp,
                            bottom = 4.dp,
                            start = 8.dp
                        ),
                    contentAlignment = contentAlignment
                ) {
                    Text(
                        text = text,
                        Modifier.basicMarquee(),
                        fontWeight = when(bold) {
                            true -> FontWeight.Bold
                            else -> FontWeight.Normal
                        },
                        maxLines = 1
                    )
                }
            }

            Column(
                Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.fillMaxWidth()
                ) {
                    TableTextBox(
                        text = "",
                        weight = 0.4f
                    )

                    TableTextBox(
                        text = stringResource(R.string.common_per_serving),
                        bold = true,
                        weight = 0.3f
                    )

                    TableTextBox(
                        text = stringResource(R.string.common_total),
                        bold = true,
                        weight = 0.15f
                    )

                    TableTextBox(
                        text = "",
                        weight = 0.15f
                    )
                }

                if(
                    foodProperties.size > 0 && (
                            recipeProperties.size == 0
                                    || showFoodProperties
                            )
                ) {
                    foodProperties.forEach {
                        HorizontalDivider()

                        Row(
                            Modifier.fillMaxWidth()
                        ) {
                            TableTextBox(
                                text = it.name,
                                weight = 0.4f,
                                contentAlignment = Alignment.CenterStart
                            )

                            TableTextBox(
                                text = (it.total_value / recipe.servings).formatAmount(
                                    showFractionalValues
                                )
                                    .ifBlank { "—" },
                                weight = 0.3f
                            )

                            TableTextBox(
                                text = (it.total_value * (servingsFactor ?: 1.0)).formatAmount(
                                    showFractionalValues
                                )
                                    .ifBlank { "—" },
                                weight = 0.15f
                            )

                            TableTextBox(
                                text = it.unit ?: "",
                                weight = 0.15f,
                                contentAlignment = Alignment.CenterStart
                            )
                        }
                    }
                } else {
                    recipeProperties.forEach {
                        HorizontalDivider()

                        Row(
                            Modifier.fillMaxWidth()
                        ) {
                            TableTextBox(
                                text = it.property_type.name,
                                weight = 0.4f,
                                contentAlignment = Alignment.CenterStart
                            )

                            TableTextBox(
                                text = (it.property_amount).formatAmount(showFractionalValues)
                                    .ifBlank { "—" },
                                weight = 0.3f
                            )

                            TableTextBox(
                                text = (it.property_amount * (servingsFactor
                                    ?: 1.0) * recipe.servings).formatAmount(showFractionalValues)
                                    .ifBlank { "—" },
                                weight = 0.15f
                            )

                            TableTextBox(
                                text = it.property_type.unit ?: "",
                                weight = 0.15f,
                                contentAlignment = Alignment.CenterStart
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipePropertiesCardFoodRecipeToggle(
    foodActive: Boolean,
    onActiveChanged: (foodActive: Boolean) -> Unit
) {
    MultiChoiceSegmentedButtonRow {
        SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            icon = {
                SegmentedButtonDefaults.Icon(active = foodActive) {
                    Icon(
                        imageVector = Icons.Rounded.Fastfood,
                        contentDescription = null,
                        modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                    )
                }
            },
            onCheckedChange = {
                onActiveChanged(it)
            },
            checked = foodActive
        ) {
            Text(stringResource(R.string.common_ingredients))
        }

        SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            icon = {
                SegmentedButtonDefaults.Icon(active = !foodActive) {
                    Icon(
                        imageVector = Icons.Rounded.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                    )
                }
            },
            onCheckedChange = {
                onActiveChanged(!it)
            },
            checked = !foodActive
        ) {
            Text(stringResource(R.string.common_recipe))
        }
    }
}