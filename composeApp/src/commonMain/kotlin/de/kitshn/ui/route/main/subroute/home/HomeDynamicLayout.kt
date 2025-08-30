package de.kitshn.ui.route.main.subroute.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBarScrollBehavior
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import de.kitshn.android.homepage.builder.HomePageBuilder
import de.kitshn.api.tandoor.TandoorRequestState
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.cache.FoodNameIdMapCache
import de.kitshn.cache.KeywordNameIdMapCache
import de.kitshn.homepage.builder.HomePageSectionEnum
import de.kitshn.homepage.model.HomePage
import de.kitshn.homepage.model.HomePageSection
import de.kitshn.isScrollingUp
import de.kitshn.removeIf
import de.kitshn.ui.component.LoadingGradientWrapper
import de.kitshn.ui.component.alert.LoadingErrorAlertPaneWrapper
import de.kitshn.ui.component.home.HomePageSectionView
import de.kitshn.ui.component.settings.SettingsListItem
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.selectionMode.SelectionModeState
import de.kitshn.ui.state.ErrorLoadingSuccessState
import de.kitshn.ui.state.foreverRememberNotSavable
import de.kitshn.ui.state.rememberErrorLoadingSuccessState
import de.kitshn.ui.state.rememberForeverScrollState
import de.kitshn.ui.view.home.search.HomeSearchState
import de.kitshn.ui.view.recipe.details.RecipeServingsAmountSaveMap
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_show_all_recipes
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDynamicLayout(
    p: RouteParameters,

    searchBarScrollBehavior: SearchBarScrollBehavior,
    selectionModeState: SelectionModeState<Int>,
    homeSearchState: HomeSearchState,

    onIsScrollingUpChanged: (isScrollingUp: Boolean) -> Unit,

    wrap: @Composable (
        @Composable (
            pv: PaddingValues,
            supportsMultiplePages: Boolean,
            background: Color,

            onSelect: (String) -> Unit
        ) -> Unit
    ) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalPlatformContext.current

    var pageLoadingState by rememberErrorLoadingSuccessState()
    val scrollState = rememberForeverScrollState(key = "RouteMainSubrouteHome/columnScrollState")

    var homePage by foreverRememberNotSavable<HomePage?>(
        key = "RouteMainSubrouteHome/homePage",
        initialValue = null
    )
    val homePageSectionList = remember { mutableStateListOf<HomePageSection>() }

    LaunchedEffect(Unit) {
        if(p.vm.tandoorClient == null) return@LaunchedEffect
        if(homePage != null) return@LaunchedEffect

        val keywordNameIdMapCache = KeywordNameIdMapCache(context, p.vm.tandoorClient!!)
        val foodNameIdMapCache = FoodNameIdMapCache(context, p.vm.tandoorClient!!)

        // retrieve keywords and foods to map names to ids
        TandoorRequestState().wrapRequest {
            if(!keywordNameIdMapCache.isValid()) keywordNameIdMapCache.update(coroutineScope)
            if(!foodNameIdMapCache.isValid()) foodNameIdMapCache.update(coroutineScope)
        }

        if(p.vm.isTest) {
            TandoorRequestState().wrapRequest {
                val section = HomePageSectionEnum.NEW.toHomePageSection(
                    keywordNameIdMapCache = keywordNameIdMapCache,
                    foodNameIdMapCache = foodNameIdMapCache
                )

                section.populate(client = p.vm.tandoorClient!!)

                homePage = HomePage(
                    sections = mutableListOf(section, section),
                    validUntil = -1
                ).apply {
                    sectionsStateList.add(section)
                    sectionsStateList.add(section)
                }
            }
        } else {
            HomePageBuilder(p.vm.tandoorClient!!).apply {
                val requestState = TandoorRequestState()
                requestState.wrapRequest {
                    homePage = this.homePage
                    build(keywordNameIdMapCache, foodNameIdMapCache)

                    pageLoadingState = ErrorLoadingSuccessState.SUCCESS
                }

                if(requestState.state == TandoorRequestStateState.ERROR)
                    pageLoadingState = ErrorLoadingSuccessState.ERROR
            }
        }
    }

    LaunchedEffect(homePageSectionList.toList()) {
        if(homePageSectionList.size < 2) return@LaunchedEffect
        pageLoadingState = ErrorLoadingSuccessState.SUCCESS
    }

    LaunchedEffect(homePage?.sectionsStateList?.toList()) {
        if(homePage == null) return@LaunchedEffect

        homePageSectionList.clear()
        homePageSectionList.addAll(homePage!!.sections)

        // keep page from loading indefinitely (prob. only happens when user has less than two recipes in space)
        if(homePage?.sectionsStateList?.size == 0)
            pageLoadingState = ErrorLoadingSuccessState.SUCCESS

        // remove deleted recipes
        homePageSectionList.forEach { section ->
            section.recipeIds.removeIf {
                !p.vm.tandoorClient!!.container.recipeOverview.contains(it)
            }
        }
    }

    val isScrollingUp = scrollState.isScrollingUp()
    LaunchedEffect(isScrollingUp) { onIsScrollingUpChanged(isScrollingUp) }

    wrap { pv, supportsMultiplePanes, background, onSelect ->
        LoadingErrorAlertPaneWrapper(loadingState = pageLoadingState) {
            LoadingGradientWrapper(
                Modifier
                    .padding(pv)
                    .nestedScroll(searchBarScrollBehavior.nestedScrollConnection),
                loadingState = pageLoadingState,
                backgroundColor = background
            ) {
                Column(
                    modifier = Modifier.verticalScroll(
                        enabled = pageLoadingState != ErrorLoadingSuccessState.LOADING,
                        state = scrollState
                    )
                ) {
                    Spacer(Modifier.height(16.dp))

                    val enableMealPlanPromotion by p.vm.settings.getEnableMealPlanPromotion.collectAsState(
                        initial = true
                    )
                    if(enableMealPlanPromotion) p.vm.tandoorClient?.let {
                        val promoteTomorrowsMealPlan by p.vm.settings.getPromoteTomorrowsMealPlan.collectAsState(
                            initial = false
                        )

                        RouteMainSubrouteHomeMealPlanPromotionSection(
                            client = it,
                            loadingState = pageLoadingState,
                            day = if(promoteTomorrowsMealPlan) {
                                MealPlanPromotionSectionDay.TOMORROW
                            } else {
                                MealPlanPromotionSectionDay.TODAY
                            }
                        ) { recipeOverview, servings ->
                            RecipeServingsAmountSaveMap[recipeOverview.id] = servings
                            onSelect(recipeOverview.id.toString())
                        }
                    }

                    if(homePageSectionList.size == 0 && pageLoadingState != ErrorLoadingSuccessState.SUCCESS) {
                        repeat(5) {
                            HomePageSectionView(
                                client = p.vm.tandoorClient,
                                loadingState = pageLoadingState,
                                onClickKeyword = {
                                    coroutineScope.launch {
                                        homeSearchState.openWithKeyword(p.vm.tandoorClient!!, it)
                                    }
                                }
                            ) { }
                        }
                    } else {
                        for(section in homePageSectionList) HomePageSectionView(
                            client = p.vm.tandoorClient,
                            section = section,
                            loadingState = pageLoadingState,
                            selectionState = selectionModeState,
                            onClickKeyword = {
                                coroutineScope.launch {
                                    homeSearchState.openWithKeyword(p.vm.tandoorClient!!, it)
                                }
                            }
                        ) {
                            onSelect(it.id.toString())
                        }
                    }

                    SettingsListItem(
                        label = { Text(text = stringResource(Res.string.action_show_all_recipes)) },
                        description = { },
                        icon = Icons.AutoMirrored.Rounded.List,
                        contentDescription = "",
                        alternativeColors = supportsMultiplePanes,
                        onClick = {
                            p.vm.mainSubNavHostController?.navigate("list")
                        }
                    )
                }
            }
        }
    }
}