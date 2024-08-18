package de.kitshn.android.ui.route.recipe.cook

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import de.kitshn.android.api.tandoor.TandoorRequestState
import de.kitshn.android.api.tandoor.TandoorRequestStateState
import de.kitshn.android.api.tandoor.model.TandoorStep
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.android.ui.component.buttons.BackButton
import de.kitshn.android.ui.component.buttons.BackButtonType
import de.kitshn.android.ui.component.model.recipe.step.RecipeStepIndicator
import de.kitshn.android.ui.route.RouteParameters
import de.kitshn.android.ui.route.recipe.cook.page.RouteRecipeCookPageDone
import de.kitshn.android.ui.route.recipe.cook.page.RouteRecipeCookPageStep
import de.kitshn.android.ui.state.foreverRememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteRecipeCook(
    p: RouteParameters
) {
    val layoutDirection = LocalLayoutDirection.current
    val coroutineScope = rememberCoroutineScope()

    val recipeId = p.bse.arguments?.getString("recipeId")
    val servings = p.bse.arguments?.getString("servings")?.toInt() ?: 1

    if(recipeId == null) {
        p.onBack?.let { it() }
        return
    }

    var recipe by remember { mutableStateOf<TandoorRecipe?>(null) }
    LaunchedEffect(recipeId) {
        TandoorRequestState().apply {
            wrapRequest {
                recipe = p.vm.tandoorClient?.recipe?.get(recipeId.toInt(), cached = true)
                p.vm.tandoorClient?.recipe?.get(recipeId.toInt())

                recipe = p.vm.tandoorClient?.recipe?.get(recipeId.toInt(), cached = true)
            }

            if(state == TandoorRequestStateState.ERROR) p.onBack?.let { it() }
        }
    }

    if(recipe == null) return

    val servingsFactor = servings.toDouble() / recipe!!.servings.toDouble()

    val sortedSteps = remember { mutableStateListOf<TandoorStep>() }
    LaunchedEffect(recipe) {
        sortedSteps.clear()
        sortedSteps.addAll(recipe!!.sortSteps())
    }

    val pagerState =
        foreverRememberPagerState(key = "RouteRecipeCook/pagerState/${recipe!!.id}") { sortedSteps.size + 1 }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(
                        type = BackButtonType.CLOSE,
                        onBack = p.onBack
                    )
                },
                title = { }
            )
        }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(
                    top = it.calculateTopPadding(),
                    start = it.calculateStartPadding(layoutDirection),
                    end = it.calculateEndPadding(layoutDirection)
                )
        ) {
            HorizontalPager(
                modifier = Modifier.weight(1f, true),
                state = pagerState
            ) { index ->
                if(index == (pagerState.pageCount - 1)) {
                    RouteRecipeCookPageDone(
                        recipe = recipe!!,
                        servings = servings
                    )
                } else {
                    val step = sortedSteps[index]
                    RouteRecipeCookPageStep(
                        vm = p.vm,
                        step = step,
                        servingsFactor = servingsFactor
                    )
                }
            }

            Box(
                Modifier.fillMaxWidth()
            ) {
                RecipeStepIndicator(
                    count = sortedSteps.size,
                    selected = pagerState.currentPage,
                    includeFinishIndicator = true,
                    bottomPadding = it.calculateBottomPadding()
                ) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(it)
                    }
                }
            }
        }
    }
}