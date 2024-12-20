package de.kitshn.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit

@Composable
expect fun MarkdownRichTextWithTimerDetection(
    modifier: Modifier = Modifier,
    timerName: String,
    markdown: String,
    fontSize: TextUnit = TextUnit.Unspecified
)