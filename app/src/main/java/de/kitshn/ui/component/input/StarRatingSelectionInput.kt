package de.kitshn.ui.component.input

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.kitshn.ui.component.icons.StarIcon

@Composable
fun StarRatingSelectionInput(
    modifier: Modifier = Modifier,
    iconModifier: Modifier? = null,
    value: Int = 0,
    onChange: (value: Int) -> Unit
) {
    @Composable
    fun StarIconButton(
        rating: Int
    ) {
        IconButton(onClick = {
            onChange(rating)
        }) {
            StarIcon(
                iconModifier ?: Modifier.size(96.dp),
                enabled = value >= rating
            )
        }
    }

    Row(
        modifier = modifier
    ) {
        StarIconButton(1)
        StarIconButton(2)
        StarIconButton(3)
        StarIconButton(4)
        StarIconButton(5)
    }
}