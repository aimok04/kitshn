package de.kitshn.ui.component.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.mohamedrejeb.richeditor.model.RichTextState
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.ic_format_h1
import kitshn.composeapp.generated.resources.ic_format_h2
import kitshn.composeapp.generated.resources.ic_format_h3
import kitshn.composeapp.generated.resources.ic_title
import org.jetbrains.compose.resources.vectorResource

internal val H1SpanStyle = SpanStyle(fontSize = 2.em, fontWeight = FontWeight.Bold)
internal val H2SpanStyle = SpanStyle(fontSize = 1.5.em, fontWeight = FontWeight.Bold)
internal val H3SpanStyle = SpanStyle(fontSize = 1.17.em, fontWeight = FontWeight.Bold)

@Composable
fun MarkdownEditorRow(
    modifier: Modifier = Modifier,
    state: RichTextState,
) {
    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        item {
            MarkdownEditorRowButton(
                onClick = {
                    state.toggleSpanStyle(H1SpanStyle)
                },
                isSelected = (
                        state.currentSpanStyle.fontSize == H1SpanStyle.fontSize
                                && state.currentSpanStyle.fontWeight == H1SpanStyle.fontWeight
                        ),
                icon = vectorResource(Res.drawable.ic_format_h1)
            )
        }

        item {
            MarkdownEditorRowButton(
                onClick = {
                    state.toggleSpanStyle(H2SpanStyle)
                },
                isSelected = (
                        state.currentSpanStyle.fontSize == H2SpanStyle.fontSize
                                && state.currentSpanStyle.fontWeight == H2SpanStyle.fontWeight
                        ),
                icon = vectorResource(Res.drawable.ic_format_h2)
            )
        }

        item {
            MarkdownEditorRowButton(
                onClick = {
                    state.toggleSpanStyle(H3SpanStyle)
                },
                isSelected = (
                        state.currentSpanStyle.fontSize == H3SpanStyle.fontSize
                                && state.currentSpanStyle.fontWeight == H3SpanStyle.fontWeight
                        ),
                icon = vectorResource(Res.drawable.ic_format_h3)
            )
        }

        item {
            MarkdownEditorRowButton(
                onClick = {
                    state.clearSpanStyles()
                },
                icon = vectorResource(Res.drawable.ic_title)
            )
        }

        item {
            VerticalDivider(
                Modifier
                    .height(24.dp)
            )
        }

        item {
            MarkdownEditorRowButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                isSelected = state.currentSpanStyle.fontWeight == FontWeight.Bold,
                icon = Icons.Outlined.FormatBold
            )
        }

        item {
            MarkdownEditorRowButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic
                        )
                    )
                },
                isSelected = state.currentSpanStyle.fontStyle == FontStyle.Italic,
                icon = Icons.Outlined.FormatItalic
            )
        }

        item {
            MarkdownEditorRowButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.LineThrough
                        )
                    )
                },
                isSelected = state.currentSpanStyle.textDecoration?.contains(TextDecoration.LineThrough) == true,
                icon = Icons.Outlined.FormatStrikethrough
            )
        }

        item {
            VerticalDivider(
                Modifier
                    .height(24.dp)
            )
        }

        item {
            MarkdownEditorRowButton(
                onClick = {
                    state.toggleUnorderedList()
                },
                isSelected = state.isUnorderedList,
                icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
            )
        }

        item {
            MarkdownEditorRowButton(
                onClick = {
                    state.toggleOrderedList()
                },
                isSelected = state.isOrderedList,
                icon = Icons.Outlined.FormatListNumbered,
            )
        }

        item {
            VerticalDivider(
                Modifier
                    .height(24.dp)
            )
        }

        item {
            MarkdownEditorRowButton(
                onClick = {
                    state.toggleCodeSpan()
                },
                isSelected = state.isCodeSpan,
                icon = Icons.Outlined.Code,
            )
        }
    }
}

@Composable
fun MarkdownEditorRowButton(
    onClick: () -> Unit,
    icon: ImageVector,
    tint: Color? = null,
    isSelected: Boolean = false,
) {
    IconButton(
        modifier = Modifier
            .focusProperties { canFocus = false },
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = if(isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onBackground
            },
        ),
    ) {
        Icon(
            icon,
            contentDescription = icon.name,
            tint = tint ?: LocalContentColor.current,
            modifier = Modifier
                .background(
                    color = if(isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    },
                    shape = CircleShape
                )
        )
    }
}