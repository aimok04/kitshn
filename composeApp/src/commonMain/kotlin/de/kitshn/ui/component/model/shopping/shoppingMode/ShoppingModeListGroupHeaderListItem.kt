package de.kitshn.ui.component.model.shopping.shoppingMode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.kitshn.ui.modifier.loadingPlaceHolder
import de.kitshn.ui.state.ErrorLoadingSuccessState
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.lorem_ipsum_short
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShoppingModeListGroupHeaderListItemPlaceholder(
    modifier: Modifier = Modifier,
    loadingState: ErrorLoadingSuccessState = ErrorLoadingSuccessState.LOADING,
    enlarge: Boolean
) {
    val fontSize = if (enlarge) 24.sp else 16.sp
    val lineHeight = if (enlarge) 22.sp else 14.sp

    val density = LocalDensity.current

    ListItem(
        modifier = modifier.fillMaxWidth(),
        colors = ListItemDefaults.colors(
            headlineColor = MaterialTheme.colorScheme.primary
        ),
        leadingContent = {
            Box(
                Modifier.padding(start = 2.dp)
                    .width(5.dp)
                    .height(
                        with(density) {
                            fontSize.toDp()
                        }
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        },
        headlineContent = {
            Text(
                text = stringResource(Res.string.lorem_ipsum_short),
                modifier = Modifier.loadingPlaceHolder(loadingState),
                fontSize = fontSize,
                lineHeight = lineHeight,
                fontWeight = FontWeight.Medium
            )
        }
    )
}

@Composable
fun ShoppingModeListGroupHeaderListItem(
    modifier: Modifier = Modifier,
    label: @Composable () -> String,
    enlarge: Boolean
) {
    val fontSize = if (enlarge) 24.sp else 16.sp
    val lineHeight = if (enlarge) 22.sp else 14.sp

    val density = LocalDensity.current
    val boxHeight = remember { with(density) { fontSize.toDp() } }

    ListItem(
        modifier = modifier.fillMaxWidth(),
        colors = ListItemDefaults.colors(
            headlineColor = MaterialTheme.colorScheme.primary
        ),
        leadingContent = {
            Box(
                Modifier.padding(start = 2.dp)
                    .width(5.dp)
                    .height(boxHeight)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        },
        headlineContent = {
            Text(
                text = label(),
                fontSize = fontSize,
                lineHeight = lineHeight,
                fontWeight = FontWeight.Medium
            )
        }
    )
}