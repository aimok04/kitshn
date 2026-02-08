package de.kitshn.ui.route.recipe.cook.page

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Comment
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.handleTandoorRequestState
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.input.StarRatingSelectionInput
import de.kitshn.ui.layout.ResponsiveSideBySideLayout
import de.kitshn.ui.theme.Typography
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_save
import kitshn.composeapp.generated.resources.common_comment
import kitshn.composeapp.generated.resources.common_done
import kitshn.composeapp.generated.resources.recipe_cook_done_description
import kitshn.composeapp.generated.resources.recipe_cook_done_title
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RouteRecipeCookPageDone(
    topPadding: Dp,
    recipe: TandoorRecipe,
    servings: Int
) {
    val coroutineScope = rememberCoroutineScope()
    val requestCookLogCreateState = rememberTandoorRequestState()

    val hapticFeedback = LocalHapticFeedback.current

    var starRatingValue by remember { mutableIntStateOf(0) }
    var commentValue by remember { mutableStateOf("") }

    var openCommentBottomSheet by remember { mutableStateOf(false) }

    fun sendRating() {
        coroutineScope.launch {
            requestCookLogCreateState.wrapRequest {
                delay(500)

                recipe.client?.cookLog?.create(
                    recipe = recipe,
                    servings = servings,
                    rating = starRatingValue,
                    comment = commentValue
                )
            }

            hapticFeedback.handleTandoorRequestState(requestCookLogCreateState)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = topPadding, bottom = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        ResponsiveSideBySideLayout(
            leftMinWidth = 400.dp,
            leftMaxWidth = 600.dp,
            rightMinWidth = 300.dp,
            rightMaxWidth = 550.dp,
            leftLayout = {
                Column(
                    (if(it) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
                        .padding(
                            start = 16.dp,
                            end = if(it) 32.dp else 16.dp,
                            top = 16.dp,
                            bottom = 16.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = recipe.loadThumbnail(),
                        contentDescription = recipe.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(24.dp))
                    )

                    Text(
                        text = stringResource(Res.string.recipe_cook_done_title),
                        Modifier.padding(16.dp),
                        style = Typography().displaySmall,
                        textAlign = TextAlign.Center
                    )

                    if(recipe.id > 0) Text(
                        text = stringResource(Res.string.recipe_cook_done_description),
                        textAlign = TextAlign.Center
                    )
                }
            }
        ) {
            // don't display rating menu when viewing shared recipe (negative id)
            if(recipe.id > 0) Row {
                if(it) VerticalDivider()

                Column(
                    (if(it) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if(!it) {
                        HorizontalDivider()
                        Spacer(Modifier.height(24.dp))
                    }

                    AnimatedContent(
                        targetState = requestCookLogCreateState.state,
                        contentAlignment = Alignment.Center
                    ) { targetState ->
                        when(targetState) {
                            TandoorRequestStateState.LOADING -> {
                                ContainedLoadingIndicator()
                            }

                            TandoorRequestStateState.SUCCESS -> {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(
                                                width = LoadingIndicatorDefaults.ContainerWidth,
                                                height = LoadingIndicatorDefaults.ContainerHeight
                                            )
                                            .fillMaxSize()
                                            .clip(LoadingIndicatorDefaults.containerShape)
                                            .background(LoadingIndicatorDefaults.containedContainerColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        modifier = Modifier
                                            .height(48.dp)
                                            .width(48.dp)
                                            .padding(12.dp),
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = stringResource(Res.string.common_done),
                                        tint = LoadingIndicatorDefaults.containedIndicatorColor
                                    )
                                }
                            }

                            else -> Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                StarRatingSelectionInput(
                                    iconModifier = if(it) Modifier
                                        .height(256.dp)
                                        .width(256.dp) else null,
                                    value = starRatingValue
                                ) {
                                    starRatingValue = it
                                }

                                Spacer(Modifier.height(24.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        enabled = requestCookLogCreateState.state != TandoorRequestStateState.LOADING,
                                        onClick = {
                                            openCommentBottomSheet = true
                                        }
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Rounded.Comment,
                                            contentDescription = stringResource(Res.string.common_comment)
                                        )
                                    }

                                    Button(
                                        enabled = requestCookLogCreateState.state != TandoorRequestStateState.LOADING,
                                        onClick = { sendRating() }
                                    ) {
                                        Text(
                                            text = stringResource(Res.string.action_save)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if(openCommentBottomSheet) ModalBottomSheet(
        onDismissRequest = {
            openCommentBottomSheet = false
        }
    ) {
        val focusRequester = remember { FocusRequester() }

        TextField(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .focusRequester(focusRequester),

            value = commentValue,
            onValueChange = {
                commentValue = it
            },

            leadingIcon = {
                Icon(
                    Icons.AutoMirrored.Rounded.Comment,
                    contentDescription = stringResource(Res.string.common_comment)
                )
            },
            label = {
                Text(stringResource(Res.string.common_comment))
            },

            keyboardActions = KeyboardActions {
                openCommentBottomSheet = false
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            )
        )

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }

    TandoorRequestErrorHandler(state = requestCookLogCreateState)
}