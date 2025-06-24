package de.kitshn.ui.component.search.chips

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.ui.component.search.AdditionalSearchSettingsChipRowState
import de.kitshn.ui.component.settings.SettingsSwitchListItem
import de.kitshn.ui.dialog.select.SelectMultipleFoodsDialog
import de.kitshn.ui.dialog.select.rememberSelectMultipleFoodsDialogState
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_ingredients
import kitshn.composeapp.generated.resources.search_food_including_all_description
import kitshn.composeapp.generated.resources.search_food_including_all_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun FoodSearchSettingChip(
    client: TandoorClient,
    state: AdditionalSearchSettingsChipRowState
) {
    val selected = state.selectedFoods.size > 0

    val dialogState = rememberSelectMultipleFoodsDialogState()
    SelectMultipleFoodsDialog(
        client = client,
        prepend = {
            SettingsSwitchListItem(
                label = {
                    Text(text = stringResource(Res.string.search_food_including_all_label))
                },
                description = {
                    Text(text = stringResource(Res.string.search_food_including_all_description))
                },
                icon = Icons.Rounded.AllInclusive,
                checked = state.foodsAnd,
                contentDescription = ""
            ) {
                state.foodsAnd = it
            }
        },
        state = dialogState
    ) {
        state.selectedFoods.clear()
        state.selectedFoods.addAll(it)

        state.update()
    }

    FilterChip(
        selected = selected,
        onClick = {
            dialogState.open(
                state.selectedFoods
            )
        },
        label = {
            Text(stringResource(Res.string.common_ingredients) + if(selected) " (${state.selectedFoods.size})" else "")
        }
    )
}