package de.kitshn.ui.component.model.shopping

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import de.kitshn.ui.modifier.loadingPlaceHolder
import de.kitshn.ui.state.ErrorLoadingSuccessState
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.lorem_ipsum_short
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShoppingListGroupHeaderListItemPlaceholder(
    modifier: Modifier = Modifier,
    loadingState: ErrorLoadingSuccessState = ErrorLoadingSuccessState.LOADING
) {
    ListItem(
        modifier = modifier.fillMaxWidth(),
        colors = ListItemDefaults.colors(
            headlineColor = MaterialTheme.colorScheme.primary
        ),
        headlineContent = {
            Text(
                text = stringResource(Res.string.lorem_ipsum_short),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.loadingPlaceHolder(loadingState)
            )
        }
    )
}

@Composable
fun ShoppingListGroupHeaderListItem(
    modifier: Modifier = Modifier,
    label: @Composable () -> String,
) {
    ListItem(
        modifier = modifier.fillMaxWidth(),
        colors = ListItemDefaults.colors(
            headlineColor = MaterialTheme.colorScheme.primary
        ),
        headlineContent = {
            Text(
                text = label(),
                fontWeight = FontWeight.SemiBold
            )
        }
    )
}