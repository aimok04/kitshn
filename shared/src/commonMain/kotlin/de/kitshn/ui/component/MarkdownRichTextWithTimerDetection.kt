package de.kitshn.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import de.kitshn.isLaunchTimerHandlerImplemented
import de.kitshn.time.TimerDetectionDefs
import de.kitshn.time.detectTimers
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.timer_detection_and_definitions
import kitshn.shared.generated.resources.timer_detection_hour_definitions
import kitshn.shared.generated.resources.timer_detection_minute_definitions
import kitshn.shared.generated.resources.timer_detection_range_qualifier_definitions
import kitshn.shared.generated.resources.timer_detection_second_definitions
import kitshn.shared.generated.resources.timer_detection_to_definitions
import org.jetbrains.compose.resources.getStringArray

class MarkdownUriHandler(
    val onTimerClick: (seconds: Int) -> Unit,
    val onTimerRangeClick: (fromSeconds: Int, toSeconds: Int) -> Unit,
    val onUriClick: (uri: String) -> Unit
) : UriHandler {
    override fun openUri(uri: String) {
        if(uri.startsWith("timer://")) {
            onTimerClick(uri.removePrefix("timer://").toInt())
            return
        } else if(uri.startsWith("timer-range://")) {
            val components = uri.removePrefix("timer-range://").split("/")
            onTimerRangeClick(components[0].toInt(), components[1].toInt())
            return
        }

        onUriClick(uri)
    }
}

@Composable
fun MarkdownRichTextWithTimerDetection(
    modifier: Modifier = Modifier.fillMaxSize(),
    timerName: String,
    markdown: String,
    fontSize: TextUnit = TextUnit.Unspecified,
    onStartTimer: (fromSeconds: Int, toSeconds: Int, timerName: String) -> Unit
) {
    val uriHandler = LocalUriHandler.current

    var md by remember { mutableStateOf("") }
    LaunchedEffect(markdown) {
        if(isLaunchTimerHandlerImplemented) {
            val defs = TimerDetectionDefs(
                hourDefs = mutableSetOf("h", "hr", "hrs", "hour", "hours")
                    .apply { addAll(getStringArray(Res.array.timer_detection_hour_definitions)) },
                andDefs = mutableSetOf<String>()
                    .apply { addAll(getStringArray(Res.array.timer_detection_and_definitions)) },
                minuteDefs = mutableSetOf("min", "mins", "minute", "minutes")
                    .apply { addAll(getStringArray(Res.array.timer_detection_minute_definitions)) },
                secondDefs = mutableSetOf("s", "sec", "secs", "second", "seconds")
                    .apply { addAll(getStringArray(Res.array.timer_detection_second_definitions)) },
                rangeDefs = mutableSetOf<String>()
                    .apply { addAll(getStringArray(Res.array.timer_detection_to_definitions)) },
                rangeQualifierDefs = mutableSetOf(
                    "about", "around", "approx", "approximately", "at most", "at least", "up to"
                ).apply { addAll(getStringArray(Res.array.timer_detection_range_qualifier_definitions)) }
            )
            md = detectTimers(markdown, defs)
        } else {
            md = markdown
        }
    }

    val markdownUriHandler = remember {
        MarkdownUriHandler(
            onTimerClick = {
                onStartTimer(it, it, timerName)
            },
            onTimerRangeClick = { from, to ->
                onStartTimer(from, to, timerName)
            },
            onUriClick = {
                uriHandler.openUri(it)
            }
        )
    }

    CompositionLocalProvider(LocalUriHandler provides markdownUriHandler) {
        val bodyLarge = MaterialTheme.typography.bodyLarge.run {
            if(fontSize.isSpecified && fontSize.isSp) {
                copy(
                    fontSize = fontSize,
                    lineHeight = (fontSize.value + 2).sp
                )
            } else {
                this
            }
        }

        val bodyMedium = MaterialTheme.typography.bodyMedium.run {
            if(fontSize.isSpecified && fontSize.isSp) {
                copy(
                    fontSize = (fontSize.value - 4).sp,
                    lineHeight = fontSize
                )
            } else {
                this
            }
        }

        SelectionContainer {
            Markdown(
                modifier = modifier,
                content = md,
                imageTransformer = Coil3ImageTransformerImpl,
                typography = markdownTypography(
                    text = bodyLarge,
                    paragraph = bodyLarge,
                    ordered = bodyLarge,
                    bullet = bodyLarge,
                    list = bodyLarge,
                    textLink = TextLinkStyles(
                        style = bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            color = MaterialTheme.colorScheme.primary
                        ).toSpanStyle()
                    ),
                    inlineCode = bodyLarge.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    code = bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    quote = bodyMedium.plus(SpanStyle(fontStyle = FontStyle.Italic))
                )
            )
        }
    }
}