package de.kitshn.ui.component.model.recipe.activity

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.model.log.TandoorCookLog
import de.kitshn.parseIsoTime
import de.kitshn.toHumanReadableDateLabel
import de.kitshn.ui.component.icons.FiveStarIconRow

@Composable
fun RecipeActivityListItem(
    modifier: Modifier = Modifier,
    cookLog: TandoorCookLog,
    expandable: Boolean = true
) {
    val isCommentBlank = (cookLog.comment ?: "").isBlank()

    var expandText by remember { mutableStateOf(false) }

    ListItem(
        modifier = if(expandable) {
            modifier.clickable { expandText = !expandText }
        } else {
            modifier
        },
        leadingContent = {
            Box(
                Modifier.size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cookLog.created_by.display_name.first() + "",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        overlineContent = {
            Text(
                text = cookLog.created_by.display_name,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        },
        headlineContent = {
            if(isCommentBlank) {
                if(cookLog.rating != null) FiveStarIconRow(
                    rating = cookLog.rating.toDouble()
                )
            } else {
                cookLog.comment?.let {
                    AnimatedContent(
                        targetState = expandText && expandable
                    ) { expandText ->
                        when(expandText) {
                            true -> Text(
                                text = it
                            )

                            else -> Text(
                                text = it,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        supportingContent = {
            Column {
                if(!isCommentBlank && cookLog.rating != null) {
                    FiveStarIconRow(
                        rating = cookLog.rating.toDouble()
                    )
                }

                Text(
                    style = MaterialTheme.typography.bodySmall,
                    text = cookLog.created_at.parseIsoTime().date
                        .toHumanReadableDateLabel()
                )
            }
        }
    )
}