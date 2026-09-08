package de.kitshn.ui.component.model.shopping

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.formatAmount
import de.kitshn.parseTandoorDate
import de.kitshn.toHumanReadableDateLabel
import de.kitshn.ui.theme.Typography
import de.kitshn.ui.theme.playfairDisplay
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.action_edit_entry
import kitshn.shared.generated.resources.action_mark_as_done
import kitshn.shared.generated.resources.common_by
import kitshn.shared.generated.resources.common_plural_portion
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun IndividualShoppingListEntryDetailCard(
    entry: TandoorShoppingListEntry,
    fractional: Boolean = true,
    onClick: () -> Unit = {},
    onClickCheck: () -> Unit = {},
    onClickEdit: () -> Unit = {}
) {
    Card(
        onClick = onClick
    ) {
        Box {
            val createdBy =
                " · ${stringResource(Res.string.common_by)} ${entry.created_by.display_name}"

            val overlineText = entry.list_recipe_data?.let {
                entry.list_recipe_data.recipe_data?.name
                    ?: entry.list_recipe_data.meal_plan_data?.title
                    ?: ""
            }

            val headlineText = when(entry.list_recipe_data?.meal_plan_data?.from_date != null) {
                true -> entry.list_recipe_data.meal_plan_data.from_date.parseTandoorDate()
                    .toHumanReadableDateLabel()

                false -> (entry.created_at?.parseTandoorDate()
                    ?.toHumanReadableDateLabel() ?: "")
            } + createdBy

            ListItem(
                modifier = Modifier.alpha(
                    when(entry.checked) {
                        true -> 0.5f
                        else -> 1f
                    }
                ),
                overlineContent = overlineText?.let { overlineText ->
                    {
                        Text(
                            text = overlineText,
                            textDecoration = if(entry.checked) {
                                TextDecoration.LineThrough
                            } else {
                                TextDecoration.None
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                headlineContent = {
                    Text(
                        text = headlineText,
                        textDecoration = if(entry.checked) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        },
                        style = Typography().bodyLarge.copy(
                            fontFamily = playfairDisplay()
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Row(
                        modifier = Modifier.horizontalScroll(
                            rememberScrollState()
                        ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if(entry.amount != 0.0) ElevatedAssistChip(
                            label = {
                                Text(
                                    text = buildString {
                                        append(entry.amount.formatAmount(fractional = fractional))

                                        if(entry.unit != null) {
                                            append(" ")
                                            append(entry.unit.name)
                                        }
                                    },
                                    textDecoration = if(entry.checked) {
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

                        if(entry.list_recipe_data != null) {
                            if(entry.amount != 0.0) VerticalDivider(
                                modifier = Modifier.height(32.dp)
                            )

                            if(entry.list_recipe_data.servings > 0.0) FilterChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = pluralStringResource(
                                            resource = Res.plurals.common_plural_portion,
                                            quantity = entry.list_recipe_data.servings.roundToInt(),
                                            entry.list_recipe_data.servings.formatAmount(fractional = true)
                                        )
                                    )
                                },
                                selected = true
                            )

                            entry.list_recipe_data.meal_plan_data?.meal_type?.let { mealType ->
                                FilterChip(
                                    onClick = { },
                                    label = { Text(text = mealType.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedLabelColor = mealType.color,
                                        selectedContainerColor = mealType.color.copy(alpha = 0.2f)
                                    ),
                                    selected = true
                                )
                            }
                        }
                    }
                },
                trailingContent = {
                    Row {
                        IconButton(
                            onClick = onClickCheck
                        ) {
                            Icon(
                                Icons.Rounded.Check,
                                stringResource(Res.string.action_mark_as_done)
                            )
                        }

                        IconButton(
                            onClick = onClickEdit
                        ) {
                            Icon(Icons.Rounded.Edit, stringResource(Res.string.action_edit_entry))
                        }
                    }
                }
            )
        }
    }
}