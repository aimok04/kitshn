package de.kitshn.android.ui.component.model.recipe

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.android.ui.theme.Typography
import de.kitshn.android.ui.theme.playfairDisplay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalRecipeCard(
    modifier: Modifier = Modifier,
    recipeOverview: TandoorRecipeOverview,
    supportingContent: @Composable () -> Unit,

    colors: CardColors = CardDefaults.elevatedCardColors(),

    onClick: (recipeOverview: TandoorRecipeOverview) -> Unit,
    onLongClick: (recipeOverview: TandoorRecipeOverview) -> Unit
) {
    Card(
        modifier = modifier,
        onClick = { onClick(recipeOverview) }
    ) {
        Box(
            Modifier.combinedClickable(
                onClick = { onClick(recipeOverview) },
                onLongClick = { onLongClick(recipeOverview) }
            )
        ) {
            ListItem(
                colors = ListItemDefaults.colors(
                    containerColor = colors.containerColor,
                    headlineColor = colors.contentColor
                ),
                leadingContent = {
                    AsyncImage(
                        model = recipeOverview.loadThumbnail(),
                        contentDescription = recipeOverview.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                },
                headlineContent = {
                    Text(
                        text = recipeOverview.name,
                        style = Typography.bodyLarge.copy(
                            fontFamily = playfairDisplay
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = supportingContent
            )
        }
    }
}