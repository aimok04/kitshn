package de.kitshn.ui.component.model.shopping.shoppingMode

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.model.TandoorFood
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.ui.component.model.shopping.ShoppingListEntryListItem
import de.kitshn.ui.component.model.shopping.ShoppingListEntryListItemPlaceholder
import de.kitshn.ui.state.ErrorLoadingSuccessState

@Composable
fun ShoppingModeListEntryListItemPlaceholder(
    modifier: Modifier = Modifier,
    loadingState: ErrorLoadingSuccessState = ErrorLoadingSuccessState.LOADING
) {
    OutlinedCard(
        modifier = modifier.padding(
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp
        )
    ) {
        Box(
            Modifier.clickable { }
        ) {
            ShoppingListEntryListItemPlaceholder(
                modifier = Modifier.padding(8.dp),
                loadingState = loadingState,
                shoppingMode = true
            )
        }
    }
}

@Composable
fun ShoppingModeListEntryListItem(
    modifier: Modifier = Modifier,

    food: TandoorFood,
    entries: List<TandoorShoppingListEntry>,

    showFractionalValues: Boolean,

    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = modifier.padding(
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp
        )
    ) {
        Box(
            Modifier.clickable {
                onClick()
            }
        ) {
            ShoppingListEntryListItem(
                modifier = Modifier.padding(8.dp),
                food = food,
                entries = entries,
                showFractionalValues = showFractionalValues,
                shoppingMode = true
            )
        }
    }
}