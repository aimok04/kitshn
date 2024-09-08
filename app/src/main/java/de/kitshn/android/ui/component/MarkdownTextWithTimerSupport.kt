package de.kitshn.android.ui.component

import android.content.Intent
import android.provider.AlarmClock
import android.text.util.Linkify
import android.widget.Toast
import androidx.annotation.FontRes
import androidx.annotation.IdRes
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import coil.ImageLoader
import de.kitshn.android.R
import dev.jeziellago.compose.markdowntext.AutoSizeConfig
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun MarkdownTextWithTimerSupport(
    timerName: String,
    markdown: String,
    modifier: Modifier = Modifier,
    linkColor: Color = MaterialTheme.colorScheme.primary,
    truncateOnTextOverflow: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    isTextSelectable: Boolean = false,
    autoSizeConfig: AutoSizeConfig? = null,
    @FontRes fontResource: Int? = null,
    style: TextStyle = LocalTextStyle.current,
    @IdRes viewId: Int? = null,
    onClick: (() -> Unit)? = null,
    // this option will disable all clicks on links, inside the markdown text
    // it also enable the parent view to receive the click event
    disableLinkMovementMethod: Boolean = false,
    imageLoader: ImageLoader? = null,
    linkifyMask: Int = Linkify.EMAIL_ADDRESSES or Linkify.PHONE_NUMBERS or Linkify.WEB_URLS,
    enableSoftBreakAddsNewLine: Boolean = true,
    onLinkClicked: ((String) -> Unit)? = null,
    onTextLayout: ((numLines: Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    var md by remember { mutableStateOf("") }
    LaunchedEffect(markdown) {
        md = markdown.replace(Regex("[0-9]+ (minuten|min|minutes|mins)", RegexOption.IGNORE_CASE)) {
            "[**⏲ ${it.value}**](timer://${it.value.split(" ")[0]})"
        }
    }

    MarkdownText(
        markdown = md,
        modifier = modifier,
        linkColor = linkColor,
        truncateOnTextOverflow = truncateOnTextOverflow,
        maxLines = maxLines,
        isTextSelectable = isTextSelectable,
        autoSizeConfig = autoSizeConfig,
        fontResource = fontResource,
        style = style,
        viewId = viewId,
        onClick = onClick,
        disableLinkMovementMethod = disableLinkMovementMethod,
        imageLoader = imageLoader,
        linkifyMask = linkifyMask,
        enableSoftBreakAddsNewLine = enableSoftBreakAddsNewLine,
        onLinkClicked = { link ->
            onLinkClicked?.let { it(link) }

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
        },
        onTextLayout = onTextLayout,
    )
}