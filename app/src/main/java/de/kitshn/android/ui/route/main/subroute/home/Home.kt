package de.kitshn.android.ui.route.main.subroute.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.TandoorRequestState
import de.kitshn.android.api.tandoor.TandoorRequestStateState
import de.kitshn.android.cache.FoodNameIdMapCache
import de.kitshn.android.cache.KeywordNameIdMapCache
import de.kitshn.android.homepage.builder.HomePageBuilder
import de.kitshn.android.homepage.model.HomePage
import de.kitshn.android.homepage.model.HomePageSection
import de.kitshn.android.isScrollingUp
import de.kitshn.android.ui.component.LoadingGradientWrapper
import de.kitshn.android.ui.component.alert.LoadingErrorAlertPaneWrapper
import de.kitshn.android.ui.component.home.HomePageSectionView
import de.kitshn.android.ui.component.settings.SettingsListItem
import de.kitshn.android.ui.component.text.DynamicGreetingTitle
import de.kitshn.android.ui.dialog.recipe.RecipeImportDialog
import de.kitshn.android.ui.dialog.recipe.creationandedit.RecipeCreationAndEditDialog
import de.kitshn.android.ui.dialog.recipe.creationandedit.rememberRecipeCreationDialogState
import de.kitshn.android.ui.dialog.recipe.creationandedit.rememberRecipeEditDialogState
import de.kitshn.android.ui.dialog.recipe.rememberRecipeImportDialogState
import de.kitshn.android.ui.layout.KitshnRecipeListRecipeDetailPaneScaffold
import de.kitshn.android.ui.route.RouteParameters
import de.kitshn.android.ui.selectionMode.model.RecipeSelectionModeTopAppBar
import de.kitshn.android.ui.selectionMode.rememberSelectionModeState
import de.kitshn.android.ui.state.ErrorLoadingSuccessState
import de.kitshn.android.ui.state.foreverRememberNotSavable
import de.kitshn.android.ui.state.rememberErrorLoadingSuccessState
import de.kitshn.android.ui.state.rememberForeverScrollState
import de.kitshn.android.ui.view.home.search.ViewHomeSearch
import de.kitshn.android.ui.view.home.search.rememberHomeSearchState
import de.kitshn.android.ui.view.recipe.details.RecipeServingsAmountSaveMap
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMainSubrouteHome(
    p: RouteParameters
) {
    val coroutineScope = rememberCoroutineScope()

    var pageLoadingState by rememberErrorLoadingSuccessState()
    val homeSearchState by rememberHomeSearchState(key = "RouteMainSubrouteHome/homeSearch")

    val recipeImportDialogState =
        rememberRecipeImportDialogState(key = "RouteMainSubrouteHome/recipeImportDialogState")

    val recipeCreationDialogState =
        rememberRecipeCreationDialogState(key = "RouteMainSubrouteHome/recipeCreationDialogState")
    val recipeEditDialogState =
        rememberRecipeEditDialogState(key = "RouteMainSubrouteHome/recipeEditDialogState")

    val selectionModeState = rememberSelectionModeState<Int>()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val scrollState = rememberForeverScrollState(key = "RouteMainSubrouteHome/columnScrollState")

    var homePage by foreverRememberNotSavable<HomePage?>(
        key = "RouteMainSubrouteHome/homePage",
        initialValue = null
    )
    val homePageSectionList = remember { mutableStateListOf<HomePageSection>() }

    LaunchedEffect(Unit) {
        if(p.vm.tandoorClient == null) return@LaunchedEffect
        if(homePage != null) return@LaunchedEffect

        HomePageBuilder(p.vm.tandoorClient!!).apply {
            val keywordNameIdMapCache = KeywordNameIdMapCache(client.context, client)
            val foodNameIdMapCache = FoodNameIdMapCache(client.context, client)

            // retrieve keywords and foods to map names to ids
            TandoorRequestState().wrapRequest {
                if(!keywordNameIdMapCache.isValid()) keywordNameIdMapCache.update(coroutineScope)
                if(!foodNameIdMapCache.isValid()) foodNameIdMapCache.update(coroutineScope)
            }

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

    LaunchedEffect(homePageSectionList.toList()) {
        if(homePageSectionList.size < 2) return@LaunchedEffect
        pageLoadingState = ErrorLoadingSuccessState.SUCCESS
    }

    LaunchedEffect(homePage?.sectionsStateList?.toList()) {
        if(homePage == null) return@LaunchedEffect

        homePageSectionList.clear()
        homePageSectionList.addAll(homePage!!.sections)

        // remove deleted recipes
        homePageSectionList.forEach { section ->
            section.recipeIds.removeIf { !p.vm.tandoorClient!!.container.recipeOverview.contains(it) }
        }
    }

    KitshnRecipeListRecipeDetailPaneScaffold(
        vm = p.vm,
        key = "RouteMainSubrouteHome",
        topBar = {
            RecipeSelectionModeTopAppBar(
                vm = p.vm,
                topAppBar = {
                    TopAppBar(
                        title = {
                            DynamicGreetingTitle()
                        },
                        actions = {
                            IconButton(onClick = {
                                homeSearchState.open()
                            }) {
                                Icon(Icons.Rounded.Search, stringResource(R.string.common_search))
                            }
                        },
                        colors = it,
                        scrollBehavior = scrollBehavior
                    )
                },
                state = selectionModeState
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Box(
                    Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SmallFloatingActionButton(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = { recipeImportDialogState.open() }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SaveAlt,
                            contentDescription = stringResource(id = R.string.action_import)
                        )
                    }
                }

                ExtendedFloatingActionButton(
                    expanded = scrollState.isScrollingUp(),
                    icon = { Icon(Icons.Rounded.Add, stringResource(id = R.string.action_add)) },
                    text = { Text(stringResource(id = R.string.action_add)) },
                    onClick = { recipeCreationDialogState.open() }
                )
            }
        },
        onClickKeyword = {
            coroutineScope.launch {
                homeSearchState.reopen {
                    homeSearchState.openWithKeyword(p.vm.tandoorClient!!, it)
                }
            }
        }
    ) { pv, _, supportsMultiplePages, background, onSelect ->
        // handle recipe passing
        p.vm.uiState.viewRecipe.WatchAndConsume {
            onSelect(it.toString())
        }

        ViewHomeSearch(
            vm = p.vm,
            state = homeSearchState
        ) {
            onSelect(it.id.toString())
        }

        LoadingErrorAlertPaneWrapper(loadingState = pageLoadingState) {
            LoadingGradientWrapper(
                Modifier
                    .padding(pv)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
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

                    p.vm.tandoorClient?.let {
                        RouteMainSubrouteHomeMealPlanPromotionSection(
                            client = it,
                            loadingState = pageLoadingState
                        ) { recipeOverview, servings ->
                            RecipeServingsAmountSaveMap[recipeOverview.id] = servings.roundToInt()
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
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        ),
                        label = { Text(text = stringResource(R.string.action_show_all_recipes)) },
                        description = { },
                        icon = Icons.AutoMirrored.Rounded.List,
                        contentDescription = "",
                        alternativeColors = supportsMultiplePages,
                        onClick = {
                            p.vm.mainSubNavHostController?.navigate("list")
                        }
                    )
                }
            }
        }
    }

    RecipeImportDialog(
        vm = p.vm,
        state = recipeImportDialogState,
        onViewRecipe = { p.vm.viewRecipe(it.id) }
    )

    if(p.vm.tandoorClient != null) {
        val ingredientsShowFractionalValues =
            p.vm.settings.getIngredientsShowFractionalValues.collectAsState(initial = true)

        RecipeCreationAndEditDialog(
            client = p.vm.tandoorClient!!,
            creationState = recipeCreationDialogState,
            editState = recipeEditDialogState,
            showFractionalValues = ingredientsShowFractionalValues.value,
            onRefresh = { },
            onViewRecipe = { p.vm.viewRecipe(it.id) }
        )
    }
}