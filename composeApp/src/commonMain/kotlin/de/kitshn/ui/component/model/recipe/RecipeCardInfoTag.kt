package de.kitshn.ui.component.model.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect

@Composable
fun RecipeCardInfoTag(
    hazeState: HazeState,
    shape: Shape = RoundedCornerShape(12.dp),
    content: @Composable () -> Unit
) {
    Surface(
        Modifier
            .padding(4.dp)
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = LocalHazeStyle.current.copy(
                    backgroundColor = Color.White.copy(alpha = 0.5f),
                    blurRadius = 16.dp
                )
            ),
        shape = shape,
        color = Color.White.copy(alpha = 0.5f),
        contentColor = MaterialTheme.colorScheme.contentColorFor(Color.White.copy(alpha = 0.5f))
    ) {
        Row(
            Modifier
                .padding(4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}