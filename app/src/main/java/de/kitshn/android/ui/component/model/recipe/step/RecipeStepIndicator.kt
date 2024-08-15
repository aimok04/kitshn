package de.kitshn.android.ui.component.model.recipe.step

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import de.kitshn.android.ui.theme.Typography

@Composable
fun RecipeStepIndicatorFinishItem(
    selected: Boolean,
    width: Dp,
    bottomPadding: Dp,
    onClick: () -> Unit
) {
    RecipeStepIndicatorItem(
        {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\uD83C\uDFC1",
                    style = Typography.headlineSmall
                )
            }
        },
        selected = selected,
        width = width,
        bottomPadding = bottomPadding
    ) {
        onClick()
    }
}

@Composable
fun RecipeStepIndicatorIntItem(
    item: Int,
    selected: Boolean,
    width: Dp,
    bottomPadding: Dp,
    onClick: () -> Unit
) {
    RecipeStepIndicatorItem(
        {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.toString(),
                    style = Typography.headlineSmall
                )
            }
        },
        selected = selected,
        width = width,
        bottomPadding = bottomPadding
    ) {
        onClick()
    }
}

@Composable
fun RecipeStepIndicatorItem(
    content: @Composable () -> Unit,
    selected: Boolean,
    width: Dp,
    bottomPadding: Dp,
    onClick: () -> Unit
) {
    val backgroundColor =
        if(selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
    val contentColor =
        if(selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer

    Surface(
        Modifier
            .width(width)
            .height(48.dp + bottomPadding)
            .clickable { onClick() },
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Box(
            Modifier.padding(bottom = bottomPadding)
        ) {
            Box(
                Modifier.height(48.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun RecipeStepIndicator(
    count: Int,
    selected: Int,
    includeFinishIndicator: Boolean,
    bottomPadding: Dp,
    onClick: (item: Int) -> Unit
) {
    val lazyRowState = rememberLazyListState()
    LaunchedEffect(selected) { lazyRowState.animateScrollToItem(selected) }

    val mCount = if(includeFinishIndicator) count + 1 else count

    BoxWithConstraints {
        val itemWidth = (this.maxWidth / mCount).coerceAtLeast(64.dp)

        LazyRow(
            Modifier
                .fillMaxWidth(),
            state = lazyRowState
        ) {
            items(count) {
                RecipeStepIndicatorIntItem(
                    item = it + 1,
                    selected = it <= selected,
                    width = itemWidth,
                    bottomPadding = bottomPadding
                ) {
                    onClick(it)
                }
            }

            if(includeFinishIndicator) item {
                RecipeStepIndicatorFinishItem(
                    selected = selected == count,
                    width = itemWidth,
                    bottomPadding = bottomPadding
                ) {
                    onClick(count)
                }
            }
        }
    }
}