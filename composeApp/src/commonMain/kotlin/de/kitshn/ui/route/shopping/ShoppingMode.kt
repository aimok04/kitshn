package de.kitshn.ui.route.shopping

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RemoveShoppingCart
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import de.kitshn.KeepScreenOn
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.cache.ShoppingListEntriesCache
import de.kitshn.cache.ShoppingListEntryOfflineActions
import de.kitshn.handleTandoorRequestState
import de.kitshn.model.route.GroupDividerShoppingListItemModel
import de.kitshn.model.route.GroupHeaderShoppingListItemModel
import de.kitshn.model.route.GroupedFoodShoppingListItemModel
import de.kitshn.model.route.ShoppingViewModel
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.LoadingGradientWrapper
import de.kitshn.ui.component.alert.FullSizeAlertPane
import de.kitshn.ui.component.buttons.BackButton
import de.kitshn.ui.component.buttons.BackButtonType
import de.kitshn.ui.component.loading.AnimatedContainedLoadingIndicator
import de.kitshn.ui.component.model.shopping.shoppingMode.ShoppingModeListEntryListItem
import de.kitshn.ui.component.model.shopping.shoppingMode.ShoppingModeListEntryListItemPlaceholder
import de.kitshn.ui.component.model.shopping.shoppingMode.ShoppingModeListGroupHeaderListItem
import de.kitshn.ui.component.model.shopping.shoppingMode.ShoppingModeListGroupHeaderListItemPlaceholder
import de.kitshn.ui.dialog.mealplan.MealPlanDetailsDialog
import de.kitshn.ui.dialog.mealplan.rememberMealPlanDetailsDialogState
import de.kitshn.ui.dialog.recipe.RecipeLinkDialog
import de.kitshn.ui.dialog.recipe.rememberRecipeLinkDialogState
import de.kitshn.ui.dialog.shopping.ShoppingListEntryDetailsBottomSheet
import de.kitshn.ui.dialog.shopping.rememberShoppingListEntryDetailsBottomSheetState
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.state.ErrorLoadingSuccessState
import de.kitshn.ui.view.ViewParameters
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_shopping_mode
import kitshn.composeapp.generated.resources.shopping_list_empty
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RouteShoppingMode(
    p: RouteParameters,
    platformContext: PlatformContext = LocalPlatformContext.current,
    cache: ShoppingListEntriesCache = remember {
        ShoppingListEntriesCache(
            platformContext, p.vm.tandoorClient!!
        )
    },
    vm: ShoppingViewModel = viewModel {
        ShoppingViewModel(
            p = p,
            cache = cache,
            additionalShoppingSettingsChipRowState = p.vm.uiState.additionalShoppingSettingsChipRowState,
            moveDoneToBottom = true
        )
    }
) {
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val layoutDirection = LocalLayoutDirection.current

    val additionalShoppingSettingsChipRowState = p.vm.uiState.additionalShoppingSettingsChipRowState

    val mealPlanDetailsDialogState = rememberMealPlanDetailsDialogState()
    val recipeLinkDialogState = rememberRecipeLinkDialogState()

    val shoppingListEntryDetailsBottomSheetState =
        rememberShoppingListEntryDetailsBottomSheetState()

    val actionRequestState = rememberTandoorRequestState()
    val entriesCheckRequestState = rememberTandoorRequestState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val enlargeShoppingMode =
        p.vm.settings.getEnlargeShoppingMode.collectAsState(initial = true)
    val ingredientsShowFractionalValues =
        p.vm.settings.getIngredientsShowFractionalValues.collectAsState(initial = true)

    val client = p.vm.tandoorClient

    // update shopping list entries
    LaunchedEffect(client) {
        if(client == null) return@LaunchedEffect

        while(true) {
            vm.update()
            delay(5000)
        }
    }

    var firstRun by remember { mutableStateOf(true) }
    LaunchedEffect(additionalShoppingSettingsChipRowState.updateState) {
        if(firstRun) {
            firstRun = false
            return@LaunchedEffect
        }

        vm.renderItems()
    }

    KeepScreenOn()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.common_shopping_mode)) },
                navigationIcon = {
                    BackButton(
                        type = BackButtonType.CLOSE,
                        onBack = p.onBack
                    )
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { pv ->
        Box(
            Modifier.padding(
                top = pv.calculateTopPadding(),
                start = pv.calculateStartPadding(layoutDirection),
                end = pv.calculateEndPadding(layoutDirection)
            )
        ) {
            LoadingGradientWrapper(
                loadingState = if(vm.loaded) ErrorLoadingSuccessState.SUCCESS else ErrorLoadingSuccessState.LOADING
            ) {
                if(vm.items.isEmpty() && vm.loaded) {
                    FullSizeAlertPane(
                        imageVector = Icons.Rounded.RemoveShoppingCart,
                        contentDescription = stringResource(Res.string.shopping_list_empty),
                        text = stringResource(Res.string.shopping_list_empty)
                    )
                } else {
                    LazyColumn(
                        Modifier.fillMaxSize()
                    ) {
                        if(!vm.loaded) {
                            item { ShoppingModeListGroupHeaderListItemPlaceholder(enlarge = enlargeShoppingMode.value) }
                            item { ShoppingModeListEntryListItemPlaceholder(enlarge = enlargeShoppingMode.value) }
                            item { ShoppingModeListEntryListItemPlaceholder(enlarge = enlargeShoppingMode.value) }
                            item { ShoppingModeListEntryListItemPlaceholder(enlarge = enlargeShoppingMode.value) }
                            item { ShoppingModeListEntryListItemPlaceholder(enlarge = enlargeShoppingMode.value) }
                            item { ShoppingModeListGroupHeaderListItemPlaceholder(enlarge = enlargeShoppingMode.value) }
                            item { ShoppingModeListEntryListItemPlaceholder(enlarge = enlargeShoppingMode.value) }
                            item { ShoppingModeListEntryListItemPlaceholder(enlarge = enlargeShoppingMode.value) }
                            item { ShoppingModeListEntryListItemPlaceholder(enlarge = enlargeShoppingMode.value) }
                        } else {
                            items(vm.items.size, key = { vm.items[it].key }) {
                                when(val item = vm.items[it]) {
                                    is GroupHeaderShoppingListItemModel -> {
                                        ShoppingModeListGroupHeaderListItem(
                                            label = { item.label },
                                            enlarge = enlargeShoppingMode.value
                                        )
                                    }

                                    is GroupedFoodShoppingListItemModel -> {
                                        ShoppingModeListEntryListItem(
                                            food = item.food,
                                            entries = item.entries,
                                            showFractionalValues = ingredientsShowFractionalValues.value,
                                            enlarge = enlargeShoppingMode.value,
                                            onClick = {
                                                if(p.vm.uiState.offlineState.isOffline) {
                                                    if(item.entries.all { entry -> entry.checked }) {
                                                        vm.executeOfflineAction(
                                                            item.entries,
                                                            ShoppingListEntryOfflineActions.UNCHECK
                                                        )
                                                    } else {
                                                        vm.executeOfflineAction(
                                                            item.entries,
                                                            ShoppingListEntryOfflineActions.CHECK
                                                        )
                                                    }

                                                    hapticFeedback.performHapticFeedback(
                                                        HapticFeedbackType.SegmentTick
                                                    )
                                                    return@ShoppingModeListEntryListItem
                                                }

                                                coroutineScope.launch {
                                                    entriesCheckRequestState.wrapRequest {
                                                        if(item.entries.all { entry -> entry.checked }) {
                                                            client!!.shopping.uncheck(item.entries)
                                                        } else {
                                                            client!!.shopping.check(item.entries)
                                                        }

                                                        vm.renderItems()
                                                    }

                                                    hapticFeedback.handleTandoorRequestState(
                                                        entriesCheckRequestState
                                                    )
                                                }
                                            },
                                            onLongClick = {
                                                shoppingListEntryDetailsBottomSheetState.open(item.entries)
                                            }
                                        )
                                    }

                                    is GroupDividerShoppingListItemModel -> {}
                                }
                            }
                        }
                    }
                }
            }

            Box(
                Modifier.fillMaxWidth()
                    .offset(y = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContainedLoadingIndicator(
                    visible = vm.shoppingListEntriesFetchRequest.state == TandoorRequestStateState.LOADING
                )
            }
        }
    }

    client?.let {
        MealPlanDetailsDialog(
            p = ViewParameters(
                vm = p.vm,
                back = p.onBack
            ),
            state = mealPlanDetailsDialogState,
            // not needed
            onUpdateList = { },
            onEdit = { }
        )

        RecipeLinkDialog(
            p = ViewParameters(
                vm = p.vm,
                back = p.onBack
            ),
            state = recipeLinkDialogState
        )

        ShoppingListEntryDetailsBottomSheet(
            client = it,
            showFractionalValues = ingredientsShowFractionalValues.value,
            state = shoppingListEntryDetailsBottomSheetState,
            isOffline = p.vm.uiState.offlineState.isOffline,
            onCheck = { entries ->
                if(p.vm.uiState.offlineState.isOffline) {
                    if(entries.all { entry -> entry.checked }) {
                        vm.executeOfflineAction(entries, ShoppingListEntryOfflineActions.UNCHECK)
                    } else {
                        vm.executeOfflineAction(entries, ShoppingListEntryOfflineActions.CHECK)
                    }

                    hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    return@ShoppingListEntryDetailsBottomSheet
                }

                coroutineScope.launch {
                    actionRequestState.wrapRequest {
                        if(entries.all { entry -> entry.checked }) {
                            client.shopping.uncheck(entries)
                        } else {
                            client.shopping.check(entries)
                        }

                        vm.renderItems()
                        vm.update()
                    }

                    hapticFeedback.handleTandoorRequestState(actionRequestState)
                }
            },
            onDelete = { entries ->
                if(p.vm.uiState.offlineState.isOffline) {
                    vm.executeOfflineAction(entries, ShoppingListEntryOfflineActions.DELETE)
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    return@ShoppingListEntryDetailsBottomSheet
                }

                coroutineScope.launch {
                    actionRequestState.wrapRequest {
                        client.shopping.delete(entries)
                        vm.renderItems()
                    }

                    hapticFeedback.handleTandoorRequestState(actionRequestState)
                }
            },
            onClickMealplan = { mealPlan -> mealPlanDetailsDialogState.open(mealPlan) },
            onClickRecipe = { recipe -> recipeLinkDialogState.open(recipe.toOverview()) },
            onUpdate = {
                coroutineScope.launch {
                    vm.renderItems()
                }
            }
        )
    }

    TandoorRequestErrorHandler(actionRequestState)
}