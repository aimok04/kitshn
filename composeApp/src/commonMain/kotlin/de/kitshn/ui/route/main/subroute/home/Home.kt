package de.kitshn.ui.route.main.subroute.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.VerticalFloatingToolbar
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import de.kitshn.Platforms
import de.kitshn.platformDetails
import de.kitshn.ui.component.AutoFetchingFundingBanner
import de.kitshn.ui.component.model.SpaceSwitchIconButton
import de.kitshn.ui.dialog.SpaceSwitchDialog
import de.kitshn.ui.dialog.recipe.creationandedit.RecipeCreationAndEditDialog
import de.kitshn.ui.dialog.recipe.creationandedit.rememberRecipeCreationDialogState
import de.kitshn.ui.dialog.recipe.creationandedit.rememberRecipeEditDialogState
import de.kitshn.ui.dialog.recipe.import.RecipeImportAIDialog
import de.kitshn.ui.dialog.recipe.import.RecipeImportType
import de.kitshn.ui.dialog.recipe.import.RecipeImportTypeBottomSheet
import de.kitshn.ui.dialog.recipe.import.RecipeImportUrlDialog
import de.kitshn.ui.dialog.recipe.import.rememberRecipeImportAIDialogState
import de.kitshn.ui.dialog.recipe.import.rememberRecipeImportTypeBottomSheetState
import de.kitshn.ui.dialog.recipe.import.rememberRecipeImportUrlDialogState
import de.kitshn.ui.layout.KitshnRecipeListRecipeDetailPaneScaffold
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.selectionMode.model.RecipeSelectionModeTopAppBar
import de.kitshn.ui.selectionMode.rememberSelectionModeState
import de.kitshn.ui.view.home.search.HomeSearchTopBar
import de.kitshn.ui.view.home.search.rememberHomeSearchState
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_add
import kitshn.composeapp.generated.resources.action_close
import kitshn.composeapp.generated.resources.action_import
import kitshn.composeapp.generated.resources.action_switch_space
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource

private val FabMediumInitialSize = 80.dp // FabMediumTokens.ContainerHeight
private val FabFinalSizeForMenu = 56.dp // FabMenuBaselineTokens.CloseButtonContainerHeight
private val FabMediumInitialCornerRadius = 20.dp // FabMediumTokens.ContainerCornerRadius (example, adjust if needed)
private val FabFinalCornerRadiusForMenu = FabFinalSizeForMenu.div(2)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RouteMainSubrouteHome(
    p: RouteParameters
) {
    val coroutineScope = rememberCoroutineScope()

    val homeSearchState by rememberHomeSearchState(key = "RouteMainSubrouteHome/homeSearch")

    // handle keyword passing
    p.vm.uiState.searchKeyword.WatchAndConsume {
        homeSearchState.openWithKeywordId(p.vm.tandoorClient!!, it)
    }

    val recipeImportTypeBottomSheetState = rememberRecipeImportTypeBottomSheetState()

    val recipeImportUrlDialogState =
        rememberRecipeImportUrlDialogState(key = "RouteMainSubrouteHome/recipeImportDialogState")
    val recipeImportAIDialogState = rememberRecipeImportAIDialogState()

    val recipeCreationDialogState =
        rememberRecipeCreationDialogState(key = "RouteMainSubrouteHome/recipeCreationDialogState")
    val recipeEditDialogState =
        rememberRecipeEditDialogState(key = "RouteMainSubrouteHome/recipeEditDialogState")

    val selectionModeState = rememberSelectionModeState<Int>()

    val searchBarScrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()

    val listState = rememberLazyListState()

    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    val fabVisible by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    var showSpaceSwitchDialog by rememberSaveable { mutableStateOf(false) }

    val wrap: @Composable (
        @Composable (
            pv: PaddingValues,
            supportsMultiplePages: Boolean,
            background: Color,

            onSelect: (String) -> Unit
        ) -> Unit
    ) -> Unit = { it ->
        KitshnRecipeListRecipeDetailPaneScaffold(
            vm = p.vm,
            key = "RouteMainSubrouteHome",
            topBar = {
                RecipeSelectionModeTopAppBar(
                    vm = p.vm,
                    topAppBar = {
                        HomeSearchTopBar(
                            vm = p.vm,
                            state = homeSearchState,
                            scrollBehavior = searchBarScrollBehavior
                        )
                    },
                    state = selectionModeState
                )
            },
            floatingActionButton = {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    FloatingActionButtonMenu(
                        expanded = fabMenuExpanded,
                        button = {
                            ToggleFloatingActionButton(
                                checked = fabMenuExpanded,
                                onCheckedChange = {
                                    fabMenuExpanded = !fabMenuExpanded
                                },
                                modifier = Modifier.semantics {
                                    traversalIndex = -1f
                                    stateDescription =
                                        if (fabMenuExpanded) "Expanded" else "Collapsed"
                                    contentDescription = "Toggle add menu"
                                }
                                    .animateFloatingActionButton(
                                        visible = fabVisible || fabMenuExpanded,
                                        alignment = Alignment.BottomEnd
                                    ),
                                containerSize = { checkedProgress ->
                                    // Interpolate from Medium size to the final menu 'close' button size
                                    androidx.compose.ui.unit.lerp(
                                        start = FabMediumInitialSize, // Start as Medium FAB
                                        stop = FabFinalSizeForMenu,   // End as the menu's close button size
                                        fraction = checkedProgress
                                    )
                                },
                                containerCornerRadius = { checkedProgress ->
                                    // Interpolate corner radius accordingly
                                    androidx.compose.ui.unit.lerp(
                                        start = FabMediumInitialCornerRadius, // Corner radius for Medium FAB
                                        stop = FabFinalCornerRadiusForMenu, // Corner radius for final close button
                                        fraction = checkedProgress
                                    )
                                },

                            ) {
                                val icon =
                                    if (fabMenuExpanded) Icons.Filled.Close else Icons.Filled.Add
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.animateIcon({ checkedProgress })
                                )
                            }
                        },
                        content = {
                            FloatingActionButtonMenuItem(
                                onClick = {
                                    showSpaceSwitchDialog = true
                                    fabMenuExpanded = false
                                },
                                icon = {
                                    Icon(
                                        Icons.Rounded.SwapHoriz,
                                        contentDescription = null
                                    )
                                },
                                text = {
                                    Text(stringResource(Res.string.action_switch_space))
                                }
                            )

                            FloatingActionButtonMenuItem(
                                onClick = {
                                    recipeImportTypeBottomSheetState.open()
                                    fabMenuExpanded = false
                                },
                                icon = {
                                    Icon(
                                        Icons.Rounded.SaveAlt,
                                        contentDescription = stringResource(Res.string.action_import)
                                    )
                                },
                                text = { Text(stringResource(Res.string.action_import)) }
                            )

                            FloatingActionButtonMenuItem(
                                onClick = {
                                    recipeCreationDialogState.open()
                                    fabMenuExpanded = false
                                },
                                icon = {
                                    Icon(
                                        Icons.Rounded.Add,
                                        stringResource(Res.string.action_add)
                                    )
                                },
                                text = { Text(stringResource(Res.string.action_add)) }
                            )

                        }
                    )

                    if (showSpaceSwitchDialog) {
                        SpaceSwitchDialog(
                            client = p.vm.tandoorClient,
                            onRefresh = {
                                p.vm.refreshApp()
                                showSpaceSwitchDialog = false
                            },
                            onDismiss = {
                                showSpaceSwitchDialog = false
                            }
                        )
                    }

                    // showing funding banner on iOS when user isn't subscribed
                    if (platformDetails.platform == Platforms.IOS && !p.vm.uiState.iosIsSubscribed) {
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

                        if (showBanner) {
                            Spacer(Modifier.height(16.dp))

                            AutoFetchingFundingBanner(
                                Modifier.widthIn(
                                    min = 100.dp,
                                    max = 600.dp
                                ).padding(
                                    start = 32.dp
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
        ) { pv, _, supportsMultiplePanes, background, onSelect ->
            // handle recipe passing
            p.vm.uiState.viewRecipe.WatchAndConsume {
                onSelect(it.toString())
            }

            it(pv, supportsMultiplePanes, background, onSelect)
        }
    }

    val enableDynamicHomeScreen by p.vm.settings.getEnableDynamicHomeScreen.collectAsState(initial = true)

    if (enableDynamicHomeScreen) {
        HomeDynamicLayout(
            p = p,
            searchBarScrollBehavior = searchBarScrollBehavior,
            selectionModeState = selectionModeState,
            homeSearchState = homeSearchState,
            wrap = wrap
        )
    } else {
        HomeTraditionalLayout(
            p = p,
            searchBarScrollBehavior = searchBarScrollBehavior,
            selectionModeState = selectionModeState,
            homeSearchState = homeSearchState,
            wrap = wrap
        )
    }

    RecipeImportTypeBottomSheet(
        client = p.vm.tandoorClient!!,
        state = recipeImportTypeBottomSheetState,
        onSelect = {
            when (it) {
                RecipeImportType.URL -> recipeImportUrlDialogState.open()
                RecipeImportType.AI -> recipeImportAIDialogState.open()
            }
        }
    )

    RecipeImportUrlDialog(
        vm = p.vm,
        state = recipeImportUrlDialogState,
        onViewRecipe = { p.vm.viewRecipe(it.id) }
    )

    RecipeImportAIDialog(
        vm = p.vm,
        state = recipeImportAIDialogState,
        onViewRecipe = { p.vm.viewRecipe(it.id) }
    )

    if (p.vm.tandoorClient != null) {
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
