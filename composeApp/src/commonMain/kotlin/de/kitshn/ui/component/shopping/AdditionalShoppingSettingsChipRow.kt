package de.kitshn.ui.component.shopping

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarket
import de.kitshn.cache.ShoppingSupermarketCache
import de.kitshn.ui.component.shopping.chips.GroupingOptions
import de.kitshn.ui.component.shopping.chips.GroupingSettingChip
import de.kitshn.ui.component.shopping.chips.SupermarketSettingChip
import kotlinx.datetime.Clock

@Composable
fun rememberAdditionalShoppingSettingsChipRowState(): AdditionalShoppingSettingsChipRowState {
    return remember {
        AdditionalShoppingSettingsChipRowState()
    }
}

class AdditionalShoppingSettingsChipRowState {

    var updateState by mutableLongStateOf(0L)
    fun update() {
        updateState = Clock.System.now().toEpochMilliseconds()
    }

    var grouping by mutableStateOf(GroupingOptions.BY_CATEGORY)
    var supermarket by mutableStateOf<TandoorSupermarket?>(null)

}

@Composable
fun AdditionalShoppingSettingsChipRow(
    client: TandoorClient,
    state: AdditionalShoppingSettingsChipRowState,
    cache: ShoppingSupermarketCache
) {
    Row(
        Modifier
            .horizontalScroll(rememberScrollState())
            .padding(top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(Modifier.width(8.dp))

        GroupingSettingChip(state)
        SupermarketSettingChip(client, state, cache)

        Spacer(Modifier.width(8.dp))
    }
}