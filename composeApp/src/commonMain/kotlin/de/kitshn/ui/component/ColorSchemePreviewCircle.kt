package de.kitshn.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.kitshn.ui.theme.custom.AvailableColorSchemes
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_selected
import org.jetbrains.compose.resources.stringResource

@Composable
fun ColorSchemePreviewCircle(
    colorScheme: AvailableColorSchemes,
    selected: Boolean = false,
    customColorSchemeSeed: Color? = null,
    onSelect: () -> Unit = { }
) {
    val colors = colorScheme.preview(customColorSchemeSeed)

    Box(
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.clip(RoundedCornerShape(16.dp))
                .clickable { onSelect() }
        ) {
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(48.dp)
                    .background(colors[0])
            )

            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(48.dp)
                    .background(colors[1])
            )
        }

        if(selected) Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = stringResource(Res.string.common_selected),
            tint = Color.White
        )
    }
}