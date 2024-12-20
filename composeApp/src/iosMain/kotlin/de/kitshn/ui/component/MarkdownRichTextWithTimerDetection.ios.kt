package de.kitshn.ui.component

import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit

@Composable
actual fun MarkdownRichTextWithTimerDetection(
    modifier: Modifier,
    timerName: String,
    markdown: String,
    fontSize: TextUnit
) {
    ProvideTextStyle(
        value = TextStyle(
            fontSize = fontSize,
            lineHeight = fontSize
        )
    ) {
        Text(
            modifier = modifier,
            text = markdown
        )
    }
}