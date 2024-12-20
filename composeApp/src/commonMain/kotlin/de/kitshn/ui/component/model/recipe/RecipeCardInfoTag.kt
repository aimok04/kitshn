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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild

@Composable
fun RecipeCardInfoTag(
    hazeState: HazeState,
    content: @Composable () -> Unit
) {
    Surface(
        Modifier
            .padding(4.dp)
            .hazeChild(hazeState),
        shape = RoundedCornerShape(12.dp),
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