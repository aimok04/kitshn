package de.kitshn.ui.component.model.recipe.step

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.model.TandoorStep
import de.kitshn.ui.theme.Typography

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
                    style = Typography().headlineSmall
                )
            }
        },
        selected = selected,
        modifier = Modifier.width(width),
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
                    style = Typography().headlineSmall
                )
            }
        },
        selected = selected,
        modifier = Modifier.width(width),
        bottomPadding = bottomPadding
    ) {
        onClick()
    }
}

@Composable
fun RecipeStepIndicatorTextItem(
    text: String,
    selected: Boolean,
    minWidth: Dp,
    maxWidth: Dp,
    bottomPadding: Dp,
    onClick: () -> Unit
) {
    RecipeStepIndicatorItem(
        {
            Box(
                modifier = Modifier.widthIn(
                    min = minWidth,
                    max = maxWidth
                ).fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                    style = Typography().titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        selected = selected,
        modifier = Modifier.widthIn(
            min = minWidth,
            max = maxWidth
        ),
        bottomPadding = bottomPadding
    ) {
        onClick()
    }
}

@Composable
fun RecipeStepIndicatorItem(
    content: @Composable () -> Unit,
    selected: Boolean,
    modifier: Modifier,
    bottomPadding: Dp,
    onClick: () -> Unit
) {
    val backgroundColor =
        if(selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
    val contentColor =
        if(selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer

    Surface(
        modifier
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
    steps: List<TandoorStep>,
    selected: Int,
    includeFinishIndicator: Boolean,
    bottomPadding: Dp,
    onClick: (item: Int) -> Unit
) {
    val lazyRowState = rememberLazyListState()
    LaunchedEffect(selected) { lazyRowState.animateScrollToItem(selected) }

    val mCount = if(includeFinishIndicator) steps.size + 1 else steps.size

    BoxWithConstraints {
        val minWidth = (this.maxWidth / mCount).coerceAtLeast(64.dp)
        val maxWidth = (this.maxWidth / 3).coerceAtMost(256.dp)

        LazyRow(
            Modifier
                .fillMaxWidth(),
            state = lazyRowState
        ) {
            items(steps.size) {
                val step = steps[it]

                if(step.name.isNotBlank()) {
                    RecipeStepIndicatorTextItem(
                        text = step.name,
                        selected = it <= selected,
                        minWidth = minWidth,
                        maxWidth = if(minWidth > maxWidth) minWidth else maxWidth,
                        bottomPadding = bottomPadding
                    ) {
                        onClick(it)
                    }
                } else {
                    RecipeStepIndicatorIntItem(
                        item = it + 1,
                        selected = it <= selected,
                        width = minWidth,
                        bottomPadding = bottomPadding
                    ) {
                        onClick(it)
                    }
                }
            }

            if(includeFinishIndicator) item {
                RecipeStepIndicatorFinishItem(
                    selected = selected == steps.size,
                    width = minWidth,
                    bottomPadding = bottomPadding
                ) {
                    onClick(steps.size)
                }
            }
        }
    }
}