package de.kitshn.ui.component.loading

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorRequestState
import de.kitshn.api.tandoor.TandoorRequestStateState

/**
 * Animated contained loading indicator used when loading more content
 *
 * @param nextPageExists Whether there is a next page
 * @param extendedRequestState The request state of the extended request
 */
fun LazyListScope.LazyListAnimatedContainedLoadingIndicator(
    nextPageExists: Boolean,
    extendedRequestState: TandoorRequestState
) {
    item {
        if(nextPageExists) Box(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContainedLoadingIndicator(
                visible = extendedRequestState.state == TandoorRequestStateState.LOADING
            )
        }
    }
}

/**
 * Animated contained loading indicator used when loading more content
 *
 * @param nextPageExists Whether there is a next page
 * @param extendedRequestState The request state of the extended request
 */
fun LazyGridScope.LazyGridAnimatedContainedLoadingIndicator(
    nextPageExists: Boolean,
    extendedRequestState: TandoorRequestState
) {
    item(span = { GridItemSpan(maxCurrentLineSpan) }) {}
    item(span = { GridItemSpan(maxCurrentLineSpan) }) {
        if(nextPageExists) Box(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContainedLoadingIndicator(
                visible = extendedRequestState.state == TandoorRequestStateState.LOADING
            )
        }
    }
}

/**
 * Animated contained loading indicator used when loading more content
 *
 * @param nextPageExists Whether there is a next page
 * @param extendedRequestState The request state of the extended request
 */
fun LazyStaggeredGridScope.LazyStaggeredGridAnimatedContainedLoadingIndicator(
    nextPageExists: Boolean,
    extendedRequestState: TandoorRequestState
) {
    item(span = StaggeredGridItemSpan.FullLine) {
        if(nextPageExists) Box(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContainedLoadingIndicator(
                visible = extendedRequestState.state == TandoorRequestStateState.LOADING
            )
        }
    }
}