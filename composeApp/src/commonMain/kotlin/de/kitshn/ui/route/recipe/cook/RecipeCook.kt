package de.kitshn.ui.route.recipe.cook

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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.savedstate.read
import de.kitshn.KeepScreenOn
import de.kitshn.api.tandoor.TandoorRequestState
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.model.TandoorStep
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.handlePagerState
import de.kitshn.ui.component.buttons.BackButton
import de.kitshn.ui.component.buttons.BackButtonType
import de.kitshn.ui.component.model.recipe.step.RecipeStepIndicator
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.route.recipe.cook.page.RouteRecipeCookPageDone
import de.kitshn.ui.route.recipe.cook.page.RouteRecipeCookPageStep
import de.kitshn.ui.state.foreverRememberPagerState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteRecipeCook(
    p: RouteParameters
) {
    val layoutDirection = LocalLayoutDirection.current
    val hapticFeedback = LocalHapticFeedback.current

    val coroutineScope = rememberCoroutineScope()

    val recipeId = p.bse.arguments?.read { getString("recipeId") }
    val servings = p.bse.arguments?.read { getString("servings") }?.toDouble() ?: 1.0

    if(recipeId == null) {
        p.onBack?.let { it() }
        return
    }

    val ingredientsShowFractionalValues =
        p.vm.settings.getIngredientsShowFractionalValues.collectAsState(initial = true)

    var recipe by remember { mutableStateOf<TandoorRecipe?>(null) }
    LaunchedEffect(recipeId) {
        TandoorRequestState().apply {
            wrapRequest {
                recipe = p.vm.tandoorClient?.recipe?.get(recipeId.toInt(), cached = true)
                p.vm.tandoorClient?.recipe?.get(recipeId.toInt())

                recipe = p.vm.tandoorClient?.recipe?.get(recipeId.toInt(), cached = true)
            }

            if(state == TandoorRequestStateState.ERROR && recipe == null) p.onBack?.let { it() }
        }
    }

    if(recipe == null) return

    val servingsFactor = servings / recipe!!.servings.toDouble()

    val sortedSteps = remember { mutableStateListOf<TandoorStep>() }
    LaunchedEffect(recipe) {
        sortedSteps.clear()
        sortedSteps.addAll(recipe!!.sortSteps())
    }

    val pagerState =
        foreverRememberPagerState(key = "RouteRecipeCook/pagerState/${recipe!!.id}") { sortedSteps.size + 1 }

    hapticFeedback.handlePagerState(pagerState)

    // ensure that the screen stays on, during the cooking
    KeepScreenOn()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    BackButton(
                        type = BackButtonType.CLOSE,
                        overlay = true,
                        onBack = p.onBack
                    )
                },
                title = { }
            )
        }
    ) {
        val topPadding = it.calculateTopPadding()

        Column(
            Modifier
                .fillMaxSize()
                .padding(
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
                        topPadding = topPadding,
                        recipe = recipe!!,
                        servings = servings.roundToInt()
                    )
                } else {
                    val step = sortedSteps[index]
                    RouteRecipeCookPageStep(
                        topPadding = topPadding,
                        vm = p.vm,
                        recipe = recipe!!,
                        step = step,
                        servingsFactor = servingsFactor,
                        showFractionalValues = ingredientsShowFractionalValues.value
                    )
                }
            }

            Box(
                Modifier.fillMaxWidth()
            ) {
                RecipeStepIndicator(
                    steps = sortedSteps,
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