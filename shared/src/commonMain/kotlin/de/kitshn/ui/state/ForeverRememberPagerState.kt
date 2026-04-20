package de.kitshn.ui.state

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect

/**
 * Static field, contains all scroll values
 */
private val SaveMap = mutableMapOf<String, KeyParamsPagerState>()

internal data class KeyParamsPagerState(
    val pageCount: Int,
    val page: Int
)

@Composable
fun foreverRememberPagerState(
    key: String,
    initialPage: Int = 0,
    pageCount: () -> Int
): PagerState {
    val pagerState = rememberPagerState(initialPage, pageCount = pageCount)
    LaunchedEffect(key, initialPage, pageCount) {
        var savedValue = SaveMap[key]
        if(savedValue?.pageCount != pageCount()) savedValue = null

        savedValue?.page?.let { pagerState.scrollToPage(it) }
    }

    DisposableEffect(Unit) {
        onDispose {
            SaveMap[key] = KeyParamsPagerState(
                pageCount = pageCount(),
                page = pagerState.currentPage
            )
        }
    }
    return pagerState
}