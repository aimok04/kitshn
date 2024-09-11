package de.kitshn.android.ui.component

import android.content.Intent
import android.provider.AlarmClock
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.material3.RichText
import com.halilibo.richtext.ui.string.RichTextStringStyle
import de.kitshn.android.R

@Composable
fun MarkdownRichTextWithTimerDetection(
    modifier: Modifier = Modifier,
    timerName: String,
    markdown: String,
    fontSize: TextUnit = TextUnit.Unspecified
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    var md by remember { mutableStateOf("") }
    LaunchedEffect(markdown) {
        md = markdown.replace(
            Regex(
                "[0-9]+ (minuten|minutes|minute|mins|min)",
                RegexOption.IGNORE_CASE
            )
        ) {
            "[**⏲ ${it.value}**](timer://${it.value.split(" ")[0]})"
        }
    }

    ProvideTextStyle(
        value = TextStyle(
            fontSize = fontSize,
            lineHeight = fontSize
        )
    ) {
        RichText(
            modifier = modifier,
            style = RichTextStyle(
                stringStyle = RichTextStringStyle(
                    linkStyle = SpanStyle(
                        textDecoration = TextDecoration.Underline,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            ),
            linkClickHandler = { link ->
                if(link.startsWith("timer://")) {
                    val minutes = link.replaceFirst("timer://", "").toInt()

                    context.startActivity(
                        Intent().apply {
                            action = AlarmClock.ACTION_SET_TIMER
                            putExtra(AlarmClock.EXTRA_LENGTH, minutes * 60)
                            putExtra(AlarmClock.EXTRA_MESSAGE, timerName)
                            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                        }
                    )

                    Toast.makeText(
                        context,
                        context.getString(R.string.recipe_step_timer_created),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    uriHandler.openUri(link)
                }
            }
        ) {
            Markdown(md)
        }
    }
}