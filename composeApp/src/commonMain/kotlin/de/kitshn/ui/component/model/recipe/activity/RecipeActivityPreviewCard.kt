package de.kitshn.ui.component.model.recipe.activity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.model.log.TandoorCookLog
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.theme.Typography
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_activity
import org.jetbrains.compose.resources.stringResource

@Composable
fun RecipeActivityPreviewCard(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors(),
    recipe: TandoorRecipe? = null,
    onClick: () -> Unit
) {
    if(recipe == null) return

    val requestState = rememberTandoorRequestState()

    var latestCookLog by remember { mutableStateOf<TandoorCookLog?>(null) }
    LaunchedEffect(recipe) {
        requestState.wrapRequest {
            val result = recipe.client!!.cookLog.list(
                recipeId = recipe.id,
                pageSize = 1,
                page = 1
            )

            if(result.count == 0) {
                latestCookLog = null
                return@wrapRequest
            }

            latestCookLog = recipe.client!!.cookLog.list(
                recipeId = recipe.id,
                page = result.count,
                pageSize = 1
            ).results.first()
        }
    }

    AnimatedVisibility(
        latestCookLog != null,
        enter = fadeIn() + slideInHorizontally()
    ) {
        Card(
            modifier = modifier,
            colors = colors,
            onClick = onClick
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(Res.string.common_activity),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = Typography().titleLarge
                )

                latestCookLog?.let {
                    RecipeActivityListItem(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp)),
                        cookLog = it,
                        expandable = false
                    )
                }

                Button(
                    modifier = Modifier.align(Alignment.End),
                    onClick = onClick
                ) {
                    Text(
                        text = "⋯"
                    )
                }
            }
        }
    }

    TandoorRequestErrorHandler(state = requestState)
}