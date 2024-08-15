package de.kitshn.android.ui.component.icons

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FiveStarIconRow(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    rating: Double
) {
    @Composable
    fun StarIconByValue(
        half: Double,
        full: Double
    ) {
        StarIcon(iconModifier, enabled = rating >= half, half = !(rating >= full))
    }

    Row {
        repeat(5) {
            StarIconByValue(
                half = it + 0.5,
                full = it + 1.0
            )
        }
    }
}