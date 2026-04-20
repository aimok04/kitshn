package de.kitshn.ui.component.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

internal val SearchBarMinWidth: Dp = 360.dp
private const val DockedExpandedTableMaxHeightScreenRatio: Float = 2f / 3f
internal val DockedExpandedTableMinHeight: Dp = 240.dp

@Composable
internal expect fun getScreenHeight(): Dp

@ExperimentalMaterial3Api
@Composable
fun AlwaysDockedSearchBar(
    inputField: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = SearchBarDefaults.dockedShape,
    colors: SearchBarColors = SearchBarDefaults.colors(),
    tonalElevation: Dp = SearchBarDefaults.TonalElevation,
    shadowElevation: Dp = SearchBarDefaults.ShadowElevation,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        shape = shape,
        color = colors.containerColor,
        contentColor = contentColorFor(colors.containerColor),
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        modifier = modifier.zIndex(1f).width(SearchBarMinWidth)
    ) {
        Column {
            inputField()

            val screenHeight = getScreenHeight()
            val maxHeight =
                remember(screenHeight) {
                    screenHeight * DockedExpandedTableMaxHeightScreenRatio
                }
            val minHeight =
                remember(maxHeight) { DockedExpandedTableMinHeight.coerceAtMost(maxHeight) }

            Column(Modifier.heightIn(min = minHeight, max = maxHeight)) {
                HorizontalDivider(color = colors.dividerColor)
                content()
            }
        }
    }
}