package de.kitshn.ui.route.recipe.cook.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import de.kitshn.R
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.input.StarRatingSelectionInput
import de.kitshn.ui.layout.ResponsiveSideBySideLayout
import de.kitshn.ui.theme.Typography
import kotlinx.coroutines.launch

@Composable
fun RouteRecipeCookPageDone(
    topPadding: Dp,
    recipe: TandoorRecipe,
    servings: Int
) {
    val coroutineScope = rememberCoroutineScope()
    val requestCookLogCreateState = rememberTandoorRequestState()

    var starRatingValue by remember { mutableIntStateOf(0) }

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
                        text = stringResource(R.string.recipe_cook_done_title),
                        Modifier.padding(16.dp),
                        style = Typography.displaySmall,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(R.string.recipe_cook_done_description),
                        textAlign = TextAlign.Center
                    )
                }
            }
        ) {
            Row {
                if(it) VerticalDivider()

                Column(
                    (if(it) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if(!it) {
                        HorizontalDivider()
                        Spacer(Modifier.height(16.dp))
                    }

                    when(requestCookLogCreateState.state) {
                        TandoorRequestStateState.LOADING -> {
                            CircularProgressIndicator()
                        }

                        TandoorRequestStateState.SUCCESS -> {
                            Icon(
                                modifier = Modifier
                                    .height(48.dp)
                                    .width(48.dp),
                                imageVector = Icons.Rounded.Check,
                                contentDescription = stringResource(R.string.common_done)
                            )
                        }

                        else -> {
                            StarRatingSelectionInput(
                                iconModifier = if(it) Modifier
                                    .height(256.dp)
                                    .width(256.dp) else null,
                                value = starRatingValue
                            ) {
                                starRatingValue = it
                            }

                            Spacer(Modifier.height(24.dp))

                            Button(
                                enabled = requestCookLogCreateState.state != TandoorRequestStateState.LOADING,
                                onClick = {
                                    coroutineScope.launch {
                                        requestCookLogCreateState.wrapRequest {
                                            recipe.client?.cookLog?.create(
                                                recipe = recipe,
                                                servings = servings,
                                                rating = starRatingValue,
                                                comment = ""
                                            )
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    text = stringResource(id = R.string.action_save)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    TandoorRequestErrorHandler(state = requestCookLogCreateState)
}