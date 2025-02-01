package de.kitshn.ui.component.model.recipe

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import de.kitshn.KITSHN_KEYWORD_FLAG_PREFIX
import de.kitshn.api.tandoor.model.TandoorKeywordOverview
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.ui.component.icons.FiveStarIconRow
import de.kitshn.ui.modifier.loadingPlaceHolder
import de.kitshn.ui.selectionMode.SelectionModeState
import de.kitshn.ui.selectionMode.values.selectionModeCardColors
import de.kitshn.ui.state.ErrorLoadingSuccessState
import de.kitshn.ui.state.translateState
import de.kitshn.ui.theme.Typography
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.lorem_ipsum_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecipeCard(
    modifier: Modifier = Modifier,
    alternateCoverSize: Boolean = false,
    fillChipRow: Boolean = false,
    recipeOverview: TandoorRecipeOverview? = null,
    loadingState: ErrorLoadingSuccessState = ErrorLoadingSuccessState.SUCCESS,
    selectionState: SelectionModeState<Int>? = null,
    additionalCardInfoTagsStart: @Composable (hazeState: HazeState) -> Unit = { },
    additionalCardInfoTagsEnd: @Composable (hazeState: HazeState) -> Unit = { },
    onClickKeyword: (keyword: TandoorKeywordOverview) -> Unit,
    onClick: (recipeOverview: TandoorRecipeOverview) -> Unit,
) {
    val context = LocalPlatformContext.current
    val imageLoader = remember { ImageLoader(context) }

    val hapticFeedback = LocalHapticFeedback.current
    val hazeState = remember { HazeState() }

    var imageLoadingState by remember {
        mutableStateOf<AsyncImagePainter.State>(
            AsyncImagePainter.State.Loading(
                null
            )
        )
    }

    val colors = CardDefaults.selectionModeCardColors(
        selected = selectionState?.selectedItems?.contains(recipeOverview?.id) ?: false,
        defaultCardColors = CardDefaults.cardColors()
    )

    val mOnLongClick: () -> Unit = {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        recipeOverview?.let { selectionState?.selectToggle(it.id) }
    }

    val mOnClick: () -> Unit = {
        if(selectionState?.isSelectionModeEnabled() == true) {
            mOnLongClick()
        } else {
            recipeOverview?.let { onClick(it) }
        }
    }

    Card(
        modifier = modifier.widthIn(100.dp, 300.dp),
        colors = colors,
        onClick = { }
    ) {
        Column(
            Modifier.combinedClickable(
                onClick = mOnClick,
                onLongClick = mOnLongClick
            )
        ) {
            Box {
                val asyncImageState = loadingState.combine(imageLoadingState.translateState())

                AsyncImage(
                    model = recipeOverview?.loadThumbnail(),
                    onState = {
                        imageLoadingState = it
                    },
                    contentDescription = recipeOverview?.name,
                    contentScale = ContentScale.Crop,
                    imageLoader = imageLoader,
                    modifier = Modifier
                        .fillMaxWidth()
                        .run {
                            if(alternateCoverSize) {
                                height(150.dp)
                            } else {
                                aspectRatio(16f / 9f)
                            }
                        }
                        .hazeSource(hazeState)
                        .loadingPlaceHolder(asyncImageState)
                )

                if(recipeOverview != null && asyncImageState != ErrorLoadingSuccessState.LOADING) Row(
                    Modifier
                        .padding(start = 4.dp, top = 4.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        additionalCardInfoTagsStart(hazeState)

                        if(recipeOverview.working_time > 0)
                            RecipeCardTimeTag(
                                hazeState = hazeState,
                                time = recipeOverview.working_time,
                                type = RecipeCardTimeTagEnum.WORKING
                            )

                        if(recipeOverview.waiting_time > 0) RecipeCardTimeTag(
                            hazeState = hazeState,
                            time = recipeOverview.waiting_time,
                            type = RecipeCardTimeTagEnum.WAITING
                        )
                    }

                    Row {
                        if(recipeOverview.rating != null) RecipeCardInfoTag(
                            hazeState = hazeState
                        ) {
                            FiveStarIconRow(
                                iconModifier = Modifier
                                    .height(16.dp)
                                    .width(16.dp),
                                rating = recipeOverview.rating
                            )
                        }

                        additionalCardInfoTagsEnd(hazeState)
                    }
                }
            }

            Column(
                Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            ) {
                Text(
                    modifier = Modifier.loadingPlaceHolder(loadingState),
                    text = recipeOverview?.name ?: stringResource(Res.string.lorem_ipsum_title),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = Typography().titleLarge
                )
            }

            LazyRow(
                Modifier
                    .padding(bottom = 16.dp)
                    .run {
                        if(fillChipRow) {
                            weight(1f, true)
                        } else {
                            this
                        }
                    },
                verticalAlignment = Alignment.Bottom
            ) {
                item {
                    Spacer(Modifier.width(16.dp))
                }

                if(recipeOverview == null || loadingState == ErrorLoadingSuccessState.LOADING) {
                    items(4) {
                        Box(
                            Modifier
                                .height(24.dp)
                                .width(72.dp)
                                .loadingPlaceHolder(loadingState)
                        )

                        Spacer(Modifier.width(16.dp))
                    }
                } else {
                    items(recipeOverview.keywords.size, key = { recipeOverview.keywords[it].id }) {
                        val keywordOverview = recipeOverview.keywords[it]
                        if(keywordOverview.label.startsWith(KITSHN_KEYWORD_FLAG_PREFIX)) return@items

                        FilterChip(
                            onClick = {
                                onClickKeyword(keywordOverview)
                            },
                            label = {
                                Text(text = keywordOverview.label)
                            },
                            selected = true
                        )

                        Spacer(Modifier.width(16.dp))
                    }
                }
            }
        }
    }
}