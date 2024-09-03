package de.kitshn.android.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign

@Composable
fun HorizontalDividerWithLabel(
    text: String
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val width = remember {
        with(density) {
            textMeasurer.measure(
                text = text
            ).size.width.toDp()
        }
    }

    BoxWithConstraints(
        Modifier.fillMaxWidth()
    ) {
        val dividerWidth = (maxWidth / 2) - width

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            HorizontalDivider(
                Modifier.width(dividerWidth)
            )

            Text(
                text = text,
                textAlign = TextAlign.Center
            )

            HorizontalDivider(
                Modifier.width(dividerWidth)
            )
        }
    }
}