package de.kitshn.ui.component

import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import de.kitshn.isLaunchTimerHandlerImplemented
import de.kitshn.launchTimerHandler
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.timer_detection_and_definitions
import kitshn.composeapp.generated.resources.timer_detection_hour_definitions
import kitshn.composeapp.generated.resources.timer_detection_minute_definitions
import org.jetbrains.compose.resources.getStringArray

class MarkdownUriHandler(
    val onTimerClick: (seconds: Int) -> Unit,
    val onUriClick: (uri: String) -> Unit
) : UriHandler {
    override fun openUri(uri: String) {
        if(uri.startsWith("timer://")) {
            onTimerClick(uri.replaceFirst("timer://", "").toInt() * 60)
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
    fontSize: TextUnit = TextUnit.Unspecified
) {
    val uriHandler = LocalUriHandler.current
    val launchTimerHandler = launchTimerHandler()

    var md by remember { mutableStateOf("") }
    LaunchedEffect(markdown) {
        if(isLaunchTimerHandlerImplemented) {
            val hourDefinitions = mutableSetOf(
                "hours",
                "hour"
            ).apply { addAll(getStringArray(Res.array.timer_detection_hour_definitions)) }
            val hourDefinitionsStr =
                hourDefinitions.sortedByDescending { it.length }.joinToString("|")

            val andDefinitions =
                mutableSetOf("and").apply { addAll(getStringArray(Res.array.timer_detection_and_definitions)) }
            val andDefinitionsStr =
                andDefinitions.sortedByDescending { it.length }.joinToString("|")

            val minuteDefinitions = mutableSetOf(
                "minutes",
                "minute",
                "mins",
                "min"
            ).apply { addAll(getStringArray(Res.array.timer_detection_minute_definitions)) }
            val minuteDefinitionsStr =
                minuteDefinitions.sortedByDescending { it.length }.joinToString("|")

            md = markdown.replace(
                Regex(
                    "(?:([0-9]+) ?(?:$hourDefinitionsStr) ?(?:$andDefinitionsStr)? ?)?([0-9]+) ?(?:$minuteDefinitionsStr)|([0-9]+) ?(?:$hourDefinitionsStr)",
                    RegexOption.IGNORE_CASE
                )
            ) {
                val hours =
                    it.groupValues[1].ifBlank { "0" }.toInt() + it.groupValues[3].ifBlank { "0" }
                        .toInt()
                val minutes = it.groupValues[2].ifBlank { "0" }.toInt()

                val totalMinutes = (hours * 60) + minutes
                "[**⏲ ${it.value}**](timer://$totalMinutes)"
            }
        } else {
            md = markdown
        }
    }

    val markdownUriHandler = remember {
        MarkdownUriHandler(
            onTimerClick = {
                launchTimerHandler(it, timerName)
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

        Markdown(
            modifier = modifier,
            content = md,
            imageTransformer = Coil3ImageTransformerImpl,
            colors = markdownColor(
                linkText = MaterialTheme.colorScheme.primary
            ),
            typography = markdownTypography(
                text = bodyLarge,
                paragraph = bodyLarge,
                ordered = bodyLarge,
                bullet = bodyLarge,
                list = bodyLarge,
                link = bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    color = MaterialTheme.colorScheme.primary
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