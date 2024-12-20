package de.kitshn.ui.component.model.recipe

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.kitshn.R
import de.kitshn.ui.theme.Typography
import dev.chrisbanes.haze.HazeState

enum class RecipeCardTimeTagEnum {
    WORKING,
    WAITING
}

@Composable
fun RecipeCardTimeTag(
    hazeState: HazeState,
    time: Int,
    type: RecipeCardTimeTagEnum
) {
    RecipeCardInfoTag(
        hazeState = hazeState
    ) {
        Icon(
            modifier = Modifier
                .height(16.dp)
                .width(16.dp),
            imageVector = when(type) {
                RecipeCardTimeTagEnum.WORKING -> Icons.Rounded.Person
                RecipeCardTimeTagEnum.WAITING -> Icons.Rounded.Pause
            },
            contentDescription = when(type) {
                RecipeCardTimeTagEnum.WORKING -> stringResource(R.string.common_time_work)
                RecipeCardTimeTagEnum.WAITING -> stringResource(R.string.common_time_wait)
            }
        )

        Spacer(Modifier.width(4.dp))

        Text(
            text = "$time ${stringResource(id = R.string.common_minute_min)}",
            style = Typography.labelMedium
        )
    }
}