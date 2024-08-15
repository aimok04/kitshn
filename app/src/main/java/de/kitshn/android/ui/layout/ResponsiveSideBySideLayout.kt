package de.kitshn.android.ui.layout

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ResponsiveSideBySideLayout(
    leftMinWidth: Dp,
    leftMaxWidth: Dp = Dp.Unspecified,
    rightMinWidth: Dp,
    rightMaxWidth: Dp = Dp.Unspecified,

    maxHeight: Dp = Dp.Unspecified,

    showDivider: Boolean = false,

    leftLayout: @Composable (enoughSpace: Boolean) -> Unit,
    rightLayout: @Composable (enoughSpace: Boolean) -> Unit
) {
    val minWidth = leftMinWidth + rightMinWidth

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
    ) {
        if(this@BoxWithConstraints.maxWidth >= minWidth
            && (maxHeight == Dp.Unspecified || this@BoxWithConstraints.maxHeight < maxHeight)
        ) {
            Row {
                Column(
                    Modifier
                        .widthIn(
                            min = leftMinWidth,
                            max = leftMaxWidth
                        )
                        .weight(1f, true)
                ) {
                    leftLayout(true)
                }

                if(showDivider) VerticalDivider(
                    Modifier.padding(start = 16.dp, end = 16.dp)
                )

                Column(
                    Modifier
                        .widthIn(
                            min = rightMinWidth,
                            max = rightMaxWidth
                        )
                        .weight(1f)
                ) {
                    rightLayout(true)
                }
            }
        } else {
            Column {
                leftLayout(false)

                if(showDivider) HorizontalDivider(
                    Modifier.padding(top = 16.dp, bottom = 16.dp)
                )

                rightLayout(false)
            }
        }
    }
}