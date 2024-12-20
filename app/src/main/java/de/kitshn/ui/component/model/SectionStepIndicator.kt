package de.kitshn.ui.component.model

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import de.kitshn.ui.theme.Typography

@Composable
fun SectionStepIndicatorItem(
    content: @Composable () -> Unit,
    selected: Boolean,
    bottomPadding: Dp,
    onClick: () -> Unit
) {
    val backgroundColor =
        if(selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
    val contentColor =
        if(selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer

    Surface(
        Modifier
            .height(48.dp + bottomPadding)
            .clickable { onClick() },
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Box(
            Modifier.padding(bottom = bottomPadding)
        ) {
            Box(
                Modifier.height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}

@Composable
fun SectionStepIndicator(
    items: List<String>,
    selected: Int,
    bottomPadding: Dp,
    onClick: (item: Int) -> Unit
) {
    val lazyRowState = rememberLazyListState()
    LaunchedEffect(selected) { lazyRowState.animateScrollToItem(selected) }

    BoxWithConstraints {
        val itemWidth = (this.maxWidth / items.size).coerceAtLeast(64.dp)

        LazyRow(
            Modifier
                .fillMaxWidth(),
            state = lazyRowState
        ) {
            items(items.size) {
                SectionStepIndicatorItem(
                    selected = it <= selected,
                    bottomPadding = bottomPadding,
                    content = {
                        Text(
                            modifier = Modifier
                                .padding(start = 4.dp, end = 4.dp)
                                .widthIn(min = itemWidth),
                            text = items[it],
                            style = Typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                ) {
                    onClick(it)
                }
            }
        }
    }
}