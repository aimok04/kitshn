package de.kitshn.android.ui.component.model.recipebook

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.model.TandoorRecipeBook
import de.kitshn.android.api.tandoor.rememberTandoorRequestState
import de.kitshn.android.ui.TandoorRequestErrorHandler
import de.kitshn.android.ui.modifier.loadingPlaceHolder
import de.kitshn.android.ui.selectionMode.SelectionModeState
import de.kitshn.android.ui.selectionMode.values.selectionModeCardColors
import de.kitshn.android.ui.state.ErrorLoadingSuccessState
import de.kitshn.android.ui.theme.Typography
import de.kitshn.android.ui.theme.playfairDisplay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalRecipeBookCard(
    modifier: Modifier = Modifier,

    recipeBook: TandoorRecipeBook? = null,
    isFavoritesBook: Boolean = false,

    loadingState: ErrorLoadingSuccessState = ErrorLoadingSuccessState.SUCCESS,
    selectionState: SelectionModeState<Int>? = null,

    leadingContent: @Composable () -> Unit = {},

    onClick: (recipeBook: TandoorRecipeBook) -> Unit = {}
) {
    val hapticFeedback = LocalHapticFeedback.current

    val colors = CardDefaults.selectionModeCardColors(
        selected = selectionState?.selectedItems?.contains(recipeBook?.id) ?: false,
        defaultCardColors = CardDefaults.elevatedCardColors()
    )

    val listEntriesState = rememberTandoorRequestState()
    LaunchedEffect(recipeBook) {
        if(recipeBook?.entries?.size == 0) listEntriesState.wrapRequest {
            recipeBook.listEntries()
        }
    }

    val mOnLongClick: () -> Unit = {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        recipeBook?.let { selectionState?.selectToggle(it.id) }
    }

    val mOnClick: () -> Unit = {
        if(selectionState?.isSelectionModeEnabled() == true) {
            mOnLongClick()
        } else {
            recipeBook?.let { onClick(it) }
        }
    }

    Card(
        modifier = modifier.loadingPlaceHolder(loadingState),
        onClick = { }
    ) {
        ListItem(
            modifier = Modifier.combinedClickable(
                onClick = mOnClick,
                onLongClick = mOnLongClick
            ),
            colors = ListItemDefaults.colors(
                containerColor = colors.containerColor,
                headlineColor = colors.contentColor
            ),
            leadingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    leadingContent()

                    if(isFavoritesBook) {
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.errorContainer,
                                    RoundedCornerShape(16.dp)
                                )
                                .height(42.dp)
                                .width(42.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Favorite,
                                contentDescription = stringResource(R.string.common_favorites),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        AsyncImage(
                            model = recipeBook?.loadThumbnail(),
                            contentDescription = recipeBook?.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .height(42.dp)
                                .width(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .loadingPlaceHolder(
                                    ErrorLoadingSuccessState.bool(
                                        (recipeBook?.entries?.size ?: 0) > 0
                                    )
                                )
                        )
                    }
                }
            },
            headlineContent = {
                Text(
                    text = if(isFavoritesBook) {
                        stringResource(id = R.string.common_favorites)
                    } else {
                        recipeBook?.name ?: stringResource(R.string.common_loading)
                    },
                    style = Typography.bodyLarge.copy(
                        fontFamily = playfairDisplay
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = if(recipeBook?.description.isNullOrBlank()) null else {
                {
                    Text(
                        text = if(isFavoritesBook) {
                            stringResource(R.string.recipe_book_favorites_description)
                        } else {
                            recipeBook?.description ?: ""
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        )
    }

    TandoorRequestErrorHandler(state = listEntriesState)
}