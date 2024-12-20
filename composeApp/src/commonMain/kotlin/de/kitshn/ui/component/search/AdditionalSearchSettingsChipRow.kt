package de.kitshn.ui.component.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorFood
import de.kitshn.api.tandoor.model.TandoorKeyword
import de.kitshn.api.tandoor.route.TandoorRecipeQueryParametersSortOrder
import de.kitshn.ui.component.search.chips.FoodSearchSettingChip
import de.kitshn.ui.component.search.chips.KeywordSearchSettingChip
import de.kitshn.ui.component.search.chips.MinimumRatingSearchSettingChip
import de.kitshn.ui.component.search.chips.NewSearchSettingChip
import de.kitshn.ui.component.search.chips.RandomSearchSettingChip
import de.kitshn.ui.component.search.chips.SortingSearchSettingChip
import kotlinx.datetime.Clock

@Composable
fun rememberAdditionalSearchSettingsChipRowState(): AdditionalSearchSettingsChipRowState {
    return remember {
        AdditionalSearchSettingsChipRowState()
    }
}

class AdditionalSearchSettingsChipRowState {

    var updateState by mutableLongStateOf(0L)
    fun update() {
        updateState = Clock.System.now().toEpochMilliseconds()
    }

    var sortOrder by mutableStateOf<TandoorRecipeQueryParametersSortOrder?>(null)

    val selectedKeywords = mutableStateListOf<TandoorKeyword>()
    val selectedFoods = mutableStateListOf<TandoorFood>()

    var keywordsAnd by mutableStateOf(true)
    var foodsAnd by mutableStateOf(true)

    var minimumRating by mutableStateOf<Int?>(null)

    var random by mutableStateOf(false)
    var new by mutableStateOf(false)

}

@Composable
fun AdditionalSearchSettingsChipRow(
    client: TandoorClient,
    state: AdditionalSearchSettingsChipRowState
) {
    val scrollState = rememberScrollState()

    Row(
        Modifier
            .horizontalScroll(scrollState)
            .padding(top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(Modifier.width(8.dp))

        SortingSearchSettingChip(state = state)

        Spacer(Modifier.width(8.dp))

        MinimumRatingSearchSettingChip(state = state)
        KeywordSearchSettingChip(client = client, state = state)
        FoodSearchSettingChip(client = client, state = state)
        NewSearchSettingChip(state = state)
        RandomSearchSettingChip(state = state)

        Spacer(Modifier.width(8.dp))
    }
}