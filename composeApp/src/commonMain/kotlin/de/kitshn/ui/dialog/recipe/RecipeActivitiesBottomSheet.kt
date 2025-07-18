package de.kitshn.ui.dialog.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.model.log.TandoorCookLog
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.handleTandoorRequestState
import de.kitshn.reachedBottom
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.loading.AnimatedContainedLoadingIndicator
import de.kitshn.ui.component.loading.LazyListAnimatedContainedLoadingIndicator
import de.kitshn.ui.component.model.recipe.activity.RecipeActivityListItem
import de.kitshn.ui.theme.Typography
import de.kitshn.ui.view.home.search.HOME_SEARCH_PAGING_SIZE
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_activity
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberRecipeActivitiesBottomSheetState(): RecipeActivitiesBottomSheetState {
    return remember {
        RecipeActivitiesBottomSheetState()
    }
}

class RecipeActivitiesBottomSheetState(
    val shown: MutableState<Boolean> = mutableStateOf(false),
    val recipe: MutableState<TandoorRecipe?> = mutableStateOf(null)
) {
    fun open(recipe: TandoorRecipe) {
        this.recipe.value = recipe
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
        this.recipe.value = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeActivitiesBottomSheet(
    client: TandoorClient,
    state: RecipeActivitiesBottomSheetState
) {
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current

    val modalBottomSheetState = rememberModalBottomSheetState()

    val recipe = state.recipe.value ?: return

    LaunchedEffect(
        state.shown.value
    ) {
        if(state.shown.value) {
            modalBottomSheetState.show()
        } else {
            modalBottomSheetState.hide()
        }
    }

    val listRequestState = rememberTandoorRequestState()
    val extendedListRequestState = rememberTandoorRequestState()

    var currentPage by rememberSaveable { mutableIntStateOf(1) }
    var nextPageExists by rememberSaveable { mutableStateOf(false) }

    val results = remember { mutableStateListOf<TandoorCookLog>() }
    LaunchedEffect(recipe) {
        currentPage = 1

        listRequestState.wrapRequest {
            client.cookLog.list(
                recipeId = recipe.id,
                pageSize = HOME_SEARCH_PAGING_SIZE,
            )
        }?.let {
            currentPage++

            nextPageExists = it.next != null

            results.clear()
            results.addAll(it.results)
        }
    }

    val lazyListState = rememberLazyListState()
    val reachedBottom by remember { derivedStateOf { lazyListState.reachedBottom(buffer = 3) } }

    var fetchNewItems by remember { mutableStateOf(false) }
    LaunchedEffect(reachedBottom) {
        if(reachedBottom) {
            fetchNewItems = true
        }
    }

    LaunchedEffect(fetchNewItems, nextPageExists) {
        while(fetchNewItems && nextPageExists) {
            if(listRequestState.state != TandoorRequestStateState.SUCCESS) {
                delay(500)
                continue
            }

            extendedListRequestState.wrapRequest {
                client.cookLog.list(
                    recipeId = recipe.id,
                    pageSize = HOME_SEARCH_PAGING_SIZE,
                    page = currentPage
                )
            }?.let {
                currentPage++

                nextPageExists = it.next != null
                results.addAll(it.results)

                if(!reachedBottom) fetchNewItems = false
            }

            hapticFeedback.handleTandoorRequestState(extendedListRequestState)

            delay(500)
        }
    }

    ModalBottomSheet(
        modifier = Modifier.padding(
            top = with(density) {
                androidx.compose.foundation.layout.WindowInsets.statusBars
                    .getTop(density)
                    .toDp() * 2
            }
        ),
        onDismissRequest = {
            state.dismiss()
        },
        sheetState = modalBottomSheetState
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.common_activity),
                style = Typography().titleLarge,
                textAlign = TextAlign.Center
            )

            Box(
                Modifier.clip(RoundedCornerShape(16.dp)),
            ) {
                LazyColumn(
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(results.size) {
                        RecipeActivityListItem(
                            modifier = Modifier.clip(
                                RoundedCornerShape(16.dp)
                            ),
                            cookLog = results[it]
                        )
                    }

                    LazyListAnimatedContainedLoadingIndicator(
                        nextPageExists = nextPageExists,
                        extendedRequestState = extendedListRequestState
                    )
                }

                Box(
                    Modifier.fillMaxHeight(0.4f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContainedLoadingIndicator(
                        visible = listRequestState.state == TandoorRequestStateState.LOADING
                    )
                }
            }
        }
    }

    TandoorRequestErrorHandler(state = listRequestState)
    TandoorRequestErrorHandler(state = extendedListRequestState)
}