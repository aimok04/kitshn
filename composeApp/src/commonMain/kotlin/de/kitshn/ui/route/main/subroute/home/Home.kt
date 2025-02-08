package de.kitshn.ui.route.main.subroute.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.kitshn.Platforms
import de.kitshn.platformDetails
import de.kitshn.ui.component.AutoFetchingFundingBanner
import de.kitshn.ui.component.model.SpaceSwitchIconButton
import de.kitshn.ui.component.text.DynamicGreetingTitle
import de.kitshn.ui.dialog.recipe.RecipeImportDialog
import de.kitshn.ui.dialog.recipe.creationandedit.RecipeCreationAndEditDialog
import de.kitshn.ui.dialog.recipe.creationandedit.rememberRecipeCreationDialogState
import de.kitshn.ui.dialog.recipe.creationandedit.rememberRecipeEditDialogState
import de.kitshn.ui.dialog.recipe.rememberRecipeImportDialogState
import de.kitshn.ui.layout.KitshnRecipeListRecipeDetailPaneScaffold
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.selectionMode.model.RecipeSelectionModeTopAppBar
import de.kitshn.ui.selectionMode.rememberSelectionModeState
import de.kitshn.ui.view.home.search.ViewHomeSearch
import de.kitshn.ui.view.home.search.rememberHomeSearchState
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_add
import kitshn.composeapp.generated.resources.action_import
import kitshn.composeapp.generated.resources.common_search
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMainSubrouteHome(
    p: RouteParameters
) {
    val coroutineScope = rememberCoroutineScope()

    val homeSearchState by rememberHomeSearchState(key = "RouteMainSubrouteHome/homeSearch")

    val recipeImportDialogState =
        rememberRecipeImportDialogState(key = "RouteMainSubrouteHome/recipeImportDialogState")

    val recipeCreationDialogState =
        rememberRecipeCreationDialogState(key = "RouteMainSubrouteHome/recipeCreationDialogState")
    val recipeEditDialogState =
        rememberRecipeEditDialogState(key = "RouteMainSubrouteHome/recipeEditDialogState")

    val selectionModeState = rememberSelectionModeState<Int>()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    var isScrollingUp by rememberSaveable { mutableStateOf(true) }

    val wrap: @Composable (
        @Composable (
            pv: PaddingValues,
            supportsMultiplePages: Boolean,
            background: Color,

            onSelect: (String) -> Unit
        ) -> Unit
    ) -> Unit = {
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
                                SpaceSwitchIconButton(
                                    client = p.vm.tandoorClient,
                                    onRefresh = {
                                        p.vm.refreshApp()
                                    }
                                )

                                IconButton(onClick = {
                                    homeSearchState.open()
                                }) {
                                    Icon(
                                        Icons.Rounded.Search,
                                        stringResource(Res.string.common_search)
                                    )
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
                                contentDescription = stringResource(Res.string.action_import)
                            )
                        }
                    }

                    ExtendedFloatingActionButton(
                        expanded = isScrollingUp,
                        icon = { Icon(Icons.Rounded.Add, stringResource(Res.string.action_add)) },
                        text = { Text(stringResource(Res.string.action_add)) },
                        onClick = { recipeCreationDialogState.open() }
                    )

                    // showing funding banner on iOS when user isn't subscribed
                    if(platformDetails.platform == Platforms.IOS && !p.vm.uiState.iosIsSubscribed) {
                        var showBanner by remember { mutableStateOf(false) }

                        val firstRunTime by p.vm.settings.getFirstRunTime.collectAsState(Long.MAX_VALUE)
                        val fundingBannerHideUntil by p.vm.settings.getFundingBannerHideUntil.collectAsState(
                            initial = -1L
                        )

                        // only show banner one day after first run and if fundingBannerHideUntil is set
                        LaunchedEffect(firstRunTime, fundingBannerHideUntil) {
                            val now = Clock.System.now().epochSeconds
                            showBanner =
                                (firstRunTime + 24 * 3600) < now && fundingBannerHideUntil < now
                        }

                        if(showBanner) {
                            AutoFetchingFundingBanner(
                                Modifier.widthIn(
                                    min = 100.dp,
                                    max = 600.dp
                                ).padding(
                                    start = 32.dp,
                                    top = 16.dp
                                ),
                                onClickSupport = {
                                    p.vm.navHostController?.navigate("ios/manageSubscription")
                                },
                                onDismiss = {
                                    p.vm.settings.setFundingBannerHideUntil(
                                        epochSeconds = Clock.System.now()
                                            .plus(24 * 7, DateTimeUnit.HOUR)
                                            .epochSeconds
                                    )
                                }
                            )
                        }
                    }
                }
            },
            onClickKeyword = {
                coroutineScope.launch {
                    homeSearchState.reopen {
                        homeSearchState.openWithKeyword(p.vm.tandoorClient!!, it)
                    }
                }
            }
        ) { pv, value, supportsMultiplePanes, background, onSelect ->
            // handle recipe passing
            p.vm.uiState.viewRecipe.WatchAndConsume {
                onSelect(it.toString())
            }

            ViewHomeSearch(
                vm = p.vm,
                state = homeSearchState,

                handleBack = supportsMultiplePanes && value != null,
                onBack = {
                    // needed for back gesture to work correctly in search view
                    onSelect(null)
                }
            ) {
                onSelect(it.id.toString())
            }

            it(pv, supportsMultiplePanes, background, onSelect)
        }
    }

    val enableDynamicHomeScreen by p.vm.settings.getEnableDynamicHomeScreen.collectAsState(initial = true)

    if(enableDynamicHomeScreen) {
        HomeDynamicLayout(
            p = p,
            scrollBehavior = scrollBehavior,
            selectionModeState = selectionModeState,
            homeSearchState = homeSearchState,
            onIsScrollingUpChanged = { isScrollingUp = it },
            wrap = wrap
        )
    } else {
        HomeTraditionalLayout(
            p = p,
            scrollBehavior = scrollBehavior,
            selectionModeState = selectionModeState,
            homeSearchState = homeSearchState,
            onIsScrollingUpChanged = { isScrollingUp = it },
            wrap = wrap
        )
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