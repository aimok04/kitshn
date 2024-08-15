package de.kitshn.android.ui.component.search.chips

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.ui.component.search.AdditionalSearchSettingsChipRowState
import de.kitshn.android.ui.component.settings.SettingsSwitchListItem
import de.kitshn.android.ui.dialog.select.SelectMultipleFoodsDialog
import de.kitshn.android.ui.dialog.select.rememberSelectMultipleFoodsDialogState

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
                contentPadding = PaddingValues(bottom = 8.dp),
                label = {
                    Text(text = stringResource(R.string.search_food_including_all_label))
                },
                description = {
                    Text(text = stringResource(R.string.search_food_including_all_description))
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
            Text(stringResource(R.string.common_ingredients) + if(selected) " (${state.selectedFoods.size})" else "")
        }
    )
}