package de.kitshn.ui.component.model.recipe

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.ui.selectionMode.SelectionModeState
import de.kitshn.ui.selectionMode.values.selectionModeListItemColors
import de.kitshn.ui.theme.Typography
import de.kitshn.ui.theme.playfairDisplay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalRecipeCardLink(
    modifier: Modifier = Modifier,
    recipeOverview: TandoorRecipeOverview,
    selectionState: SelectionModeState<Int>? = null,
    onClick: (recipeOverview: TandoorRecipeOverview) -> Unit
) {
    val context = LocalPlatformContext.current
    val imageLoader = remember { ImageLoader(context) }

    val hapticFeedback = LocalHapticFeedback.current

    val colors = ListItemDefaults.selectionModeListItemColors(
        selected = selectionState?.selectedItems?.contains(recipeOverview.id) ?: false,
    )

    val mOnLongClick: () -> Unit = {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        recipeOverview.let { selectionState?.selectToggle(it.id) }
    }

    val mOnClick: () -> Unit = {
        if(selectionState?.isSelectionModeEnabled() == true) {
            mOnLongClick()
        } else {
            onClick(recipeOverview)
        }
    }

    Card(
        modifier = modifier,
        onClick = { }
    ) {
        Box(
            Modifier.combinedClickable(
                onClick = mOnClick,
                onLongClick = mOnLongClick
            )
        ) {
            ListItem(
                colors = colors,
                leadingContent = {
                    AsyncImage(
                        model = recipeOverview.loadThumbnail(),
                        contentDescription = recipeOverview.name,
                        contentScale = ContentScale.Crop,
                        imageLoader = imageLoader,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                },
                headlineContent = {
                    Text(
                        text = recipeOverview.name,
                        style = Typography().bodyLarge.copy(
                            fontFamily = playfairDisplay()
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    if(!recipeOverview.description.isNullOrBlank()) {
                        Text(
                            text = recipeOverview.description,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            )
        }
    }
}