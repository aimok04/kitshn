@file:OptIn(ExperimentalTime::class)

package de.kitshn.ui.route.main.subroute.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.SyncAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import de.kitshn.Platforms
import de.kitshn.TestTagRepository
import de.kitshn.api.tandoor.route.TandoorRecipeQueryParametersSortOrder
import de.kitshn.platformDetails
import de.kitshn.ui.component.AutoFetchingFundingBanner
import de.kitshn.ui.component.model.SpaceSwitchIconButton
import de.kitshn.ui.dialog.recipe.creationandedit.RecipeCreationAndEditDialog
import de.kitshn.ui.dialog.recipe.creationandedit.rememberRecipeCreationDialogState
import de.kitshn.ui.dialog.recipe.creationandedit.rememberRecipeEditDialogState
import de.kitshn.ui.dialog.recipe.import.RecipeImportAIDialog
import de.kitshn.ui.dialog.recipe.import.RecipeImportSocialMediaDialog
import de.kitshn.ui.dialog.recipe.import.RecipeImportType
import de.kitshn.ui.dialog.recipe.import.RecipeImportTypeBottomSheet
import de.kitshn.ui.dialog.recipe.import.RecipeImportUrlDialog
import de.kitshn.ui.dialog.recipe.import.rememberRecipeImportAIDialogState
import de.kitshn.ui.dialog.recipe.import.rememberRecipeImportSocialMediaDialogState
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
import kitshn.composeapp.generated.resources.action_import
import kitshn.composeapp.generated.resources.action_remove
import kitshn.composeapp.generated.resources.common_sorting
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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

    // handle createdBy passing
    p.vm.uiState.searchCreatedBy.WatchAndConsume {
        homeSearchState.openWithCreatedById(p.vm.tandoorClient!!, it)
    }

    val recipeImportTypeBottomSheetState = rememberRecipeImportTypeBottomSheetState()

    val recipeImportUrlDialogState =
        rememberRecipeImportUrlDialogState(key = "RouteMainSubrouteHome/recipeImportDialogState")
    val recipeImportAIDialogState = rememberRecipeImportAIDialogState()
    val recipeImportSocialMediaDialogState = rememberRecipeImportSocialMediaDialogState()

    val recipeCreationDialogState =
        rememberRecipeCreationDialogState(key = "RouteMainSubrouteHome/recipeCreationDialogState")
    val recipeEditDialogState =
        rememberRecipeEditDialogState(key = "RouteMainSubrouteHome/recipeEditDialogState")

    val selectionModeState = rememberSelectionModeState<Int>()

    val searchBarScrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()

    var isScrollingUp by rememberSaveable { mutableStateOf(true) }

    val enableDynamicHomeScreen by p.vm.settings.getEnableDynamicHomeScreen.collectAsState(initial = true)
    val homeScreenSorting by p.vm.settings.getHomeScreenSorting.collectAsState(initial = "")

    var showSortingSelectionDialog by rememberSaveable { mutableStateOf(false) }

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
            topBar = { colors ->
                RecipeSelectionModeTopAppBar(
                    vm = p.vm,
                    topAppBar = {
                        HomeSearchTopBar(
                            vm = p.vm,
                            state = homeSearchState,
                            colors = colors,
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
                    HorizontalFloatingToolbar(
                        expanded = isScrollingUp,
                        content = {
                            if(!enableDynamicHomeScreen) IconButton(
                                onClick = { showSortingSelectionDialog = true }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.Sort,
                                    stringResource(Res.string.common_sorting)
                                )
                            }

                            SpaceSwitchIconButton(
                                client = p.vm.tandoorClient
                            ) {
                                p.vm.refreshApp()
                            }

                            IconButton(
                                onClick = {
                                    recipeImportTypeBottomSheetState.open()
                                }
                            ) {
                                Icon(
                                    Icons.Rounded.SaveAlt,
                                    contentDescription = stringResource(Res.string.action_import)
                                )
                            }
                        },
                        floatingActionButton = {
                            FloatingToolbarDefaults.StandardFloatingActionButton(
                                modifier = Modifier.testTag(TestTagRepository.ACTION_ADD.name),
                                onClick = {
                                    recipeCreationDialogState.open()
                                }
                            ) {
                                Icon(Icons.Rounded.Add, stringResource(Res.string.action_add))
                            }
                        }
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
            onClickUser = {
                coroutineScope.launch {
                    delay(500)

                    homeSearchState.reopen {
                        homeSearchState.openWithCreatedBy(it)
                    }
                }
            },
            onClickKeyword = {
                coroutineScope.launch {
                    delay(500)

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

    if(enableDynamicHomeScreen) {
        HomeDynamicLayout(
            p = p,
            searchBarScrollBehavior = searchBarScrollBehavior,
            selectionModeState = selectionModeState,
            homeSearchState = homeSearchState,
            onIsScrollingUpChanged = { isScrollingUp = it },
            wrap = wrap
        )
    } else {
        HomeTraditionalLayout(
            p = p,
            searchBarScrollBehavior = searchBarScrollBehavior,
            selectionModeState = selectionModeState,
            homeSearchState = homeSearchState,
            onIsScrollingUpChanged = { isScrollingUp = it },
            wrap = wrap
        )
    }

    RecipeImportTypeBottomSheet(
        client = p.vm.tandoorClient!!,
        state = recipeImportTypeBottomSheetState,
        onSelect = {
            when(it) {
                RecipeImportType.URL -> recipeImportUrlDialogState.open()
                RecipeImportType.AI -> recipeImportAIDialogState.open()
                RecipeImportType.SOCIAL_MEDIA -> recipeImportSocialMediaDialogState.open()
            }
        }
    )

    // handle import recipe url passing
    p.vm.uiState.importRecipeUrl.WatchAndConsume {
        recipeImportUrlDialogState.dismiss()
        recipeImportAIDialogState.dismiss()
        recipeImportSocialMediaDialogState.dismiss()
        delay(50)
        recipeImportUrlDialogState.open(url = it, autoFetch = true)
    }

    RecipeImportUrlDialog(
        vm = p.vm,
        state = recipeImportUrlDialogState,
        onViewRecipe = { p.vm.viewRecipe(it.id) },
        onSocialMediaImport = {
            recipeImportSocialMediaDialogState.open(
                url = it,
                autoFetch = true
            )
        }
    )

    RecipeImportAIDialog(
        vm = p.vm,
        state = recipeImportAIDialogState,
        onViewRecipe = { p.vm.viewRecipe(it.id) }
    )

    RecipeImportSocialMediaDialog(
        vm = p.vm,
        state = recipeImportSocialMediaDialogState,
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
            onRefresh = { }
        )
    }

    if(showSortingSelectionDialog) AlertDialog(
        modifier = Modifier.padding(top = 24.dp, bottom = 24.dp),
        onDismissRequest = {
            showSortingSelectionDialog = false
        },
        icon = {
            Icon(Icons.Rounded.SyncAlt, stringResource(Res.string.common_sorting))
        },
        title = {
            Text(text = stringResource(Res.string.common_sorting))
        },
        text = {
            Column(
                Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .verticalScroll(rememberScrollState())
            ) {
                TandoorRecipeQueryParametersSortOrder.entries.forEach {
                    ListItem(
                        modifier = Modifier.clickable {
                            showSortingSelectionDialog = false
                            p.vm.settings.setHomeScreenSorting(it.name)
                        },
                        headlineContent = {
                            Text(text = it.itemLabel())
                        }
                    )
                }
            }
        },
        dismissButton = {
            if(homeScreenSorting.isNotBlank()) FilledTonalButton(onClick = {
                showSortingSelectionDialog = false
                p.vm.settings.setHomeScreenSorting("")
            }) {
                Text(text = stringResource(Res.string.action_remove))
            }
        },
        confirmButton = { }
    )
}