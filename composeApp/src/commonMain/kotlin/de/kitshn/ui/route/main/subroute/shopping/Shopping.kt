package de.kitshn.ui.route.main.subroute.shopping

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.RemoveShoppingCart
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.FloatingToolbarDefaults.floatingToolbarVerticalNestedScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalFloatingToolbar
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.cache.ShoppingListEntriesCache
import de.kitshn.cache.ShoppingListEntryOfflineActions
import de.kitshn.cache.ShoppingSupermarketCache
import de.kitshn.handleTandoorRequestState
import de.kitshn.model.route.GroupDividerShoppingListItemModel
import de.kitshn.model.route.GroupHeaderShoppingListItemModel
import de.kitshn.model.route.GroupedFoodShoppingListItemModel
import de.kitshn.model.route.ShoppingViewModel
import de.kitshn.removeIf
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.LoadingGradientWrapper
import de.kitshn.ui.component.alert.FullSizeAlertPane
import de.kitshn.ui.component.icons.IconWithState
import de.kitshn.ui.component.loading.AnimatedContainedLoadingIndicator
import de.kitshn.ui.component.model.shopping.ShoppingListEntryListItem
import de.kitshn.ui.component.model.shopping.ShoppingListEntryListItemPlaceholder
import de.kitshn.ui.component.model.shopping.ShoppingListGroupHeaderListItem
import de.kitshn.ui.component.model.shopping.ShoppingListGroupHeaderListItemPlaceholder
import de.kitshn.ui.component.shopping.AdditionalShoppingSettingsChipRow
import de.kitshn.ui.dialog.mealplan.MealPlanDetailsDialog
import de.kitshn.ui.dialog.mealplan.rememberMealPlanDetailsDialogState
import de.kitshn.ui.dialog.recipe.RecipeLinkDialog
import de.kitshn.ui.dialog.recipe.rememberRecipeLinkDialogState
import de.kitshn.ui.dialog.shopping.ShoppingListEntriesClearDialog
import de.kitshn.ui.dialog.shopping.ShoppingListEntryCreationDialog
import de.kitshn.ui.dialog.shopping.ShoppingListEntryDetailsBottomSheet
import de.kitshn.ui.dialog.shopping.rememberShoppingListEntriesClearDialogState
import de.kitshn.ui.dialog.shopping.rememberShoppingListEntryCreationDialogState
import de.kitshn.ui.dialog.shopping.rememberShoppingListEntryDetailsBottomSheetState
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.selectionMode.component.SelectionModeTopAppBar
import de.kitshn.ui.selectionMode.rememberSelectionModeState
import de.kitshn.ui.state.ErrorLoadingSuccessState
import de.kitshn.ui.view.ViewParameters
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_add
import kitshn.composeapp.generated.resources.action_clear
import kitshn.composeapp.generated.resources.action_delete
import kitshn.composeapp.generated.resources.action_mark_as_done
import kitshn.composeapp.generated.resources.action_start_shopping_mode
import kitshn.composeapp.generated.resources.navigation_shopping
import kitshn.composeapp.generated.resources.shopping_list_empty
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RouteMainSubrouteShopping(
    p: RouteParameters,
    platformContext: PlatformContext = LocalPlatformContext.current,
    cache: ShoppingListEntriesCache = remember {
        ShoppingListEntriesCache(
            platformContext, p.vm.tandoorClient!!
        )
    },
    supermarketCache: ShoppingSupermarketCache = remember {
        ShoppingSupermarketCache(
            platformContext, p.vm.tandoorClient!!
        )
    },
    vm: ShoppingViewModel = viewModel {
        ShoppingViewModel(
            p = p,
            cache = cache,
            additionalShoppingSettingsChipRowState = p.vm.uiState.additionalShoppingSettingsChipRowState
        )
    }
) {
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    val additionalShoppingSettingsChipRowState = p.vm.uiState.additionalShoppingSettingsChipRowState

    val mealPlanDetailsDialogState = rememberMealPlanDetailsDialogState()
    val recipeLinkDialogState = rememberRecipeLinkDialogState()

    val shoppingListEntriesClearDialogState = rememberShoppingListEntriesClearDialogState()
    val shoppingListEntryCreationDialogState = rememberShoppingListEntryCreationDialogState()
    val shoppingListEntryDetailsBottomSheetState =
        rememberShoppingListEntryDetailsBottomSheetState()

    val entriesClearRequestState = rememberTandoorRequestState()
    val entriesDeleteRequestState = rememberTandoorRequestState()
    val entriesCheckRequestState = rememberTandoorRequestState()

    val actionRequestState = rememberTandoorRequestState()

    val selectionModeState = rememberSelectionModeState<Int>()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

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

    Scaffold(
        topBar = {
            SelectionModeTopAppBar(
                topAppBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(Res.string.navigation_shopping)) },
                        scrollBehavior = scrollBehavior
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                entriesCheckRequestState.wrapRequest {
                                    selectionModeState.selectedItems
                                        .flatMap { id -> vm.entries.filter { it.food.id == id } }
                                        .let {
                                            client!!.shopping.check(it)

                                            repeat(it.size) {
                                                hapticFeedback.performHapticFeedback(
                                                    HapticFeedbackType.SegmentTick
                                                )
                                                delay(25)
                                            }
                                        }

                                    vm.renderItems()
                                    selectionModeState.disable()
                                }
                            }
                        }
                    ) {
                        IconWithState(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = stringResource(Res.string.action_mark_as_done),
                            state = entriesCheckRequestState.state.toIconWithState()
                        )
                    }

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                entriesDeleteRequestState.wrapRequest {
                                    selectionModeState.selectedItems
                                        .flatMap { id -> vm.entries.filter { it.food.id == id } }
                                        .let {
                                            client!!.shopping.delete(it)

                                            repeat(it.size) {
                                                hapticFeedback.performHapticFeedback(
                                                    HapticFeedbackType.SegmentTick
                                                )
                                                delay(25)
                                            }
                                        }

                                    vm.renderItems()
                                    selectionModeState.disable()
                                }
                            }
                        }
                    ) {
                        IconWithState(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = stringResource(Res.string.action_delete),
                            state = entriesDeleteRequestState.state.toIconWithState()
                        )
                    }
                },
                state = selectionModeState
            )
        }
    ) { pv ->
        var expandedToolbar by remember { mutableStateOf(true) }

        Box(
            Modifier.padding(pv)
        ) {
            Column {
                if(client != null) AdditionalShoppingSettingsChipRow(
                    client = client,
                    state = additionalShoppingSettingsChipRowState,
                    cache = supermarketCache
                )

                HorizontalDivider()

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
                            Modifier
                                .fillMaxSize()
                                .then(
                                    if (vm.loaded) { // Prevent scrolling if content has not loaded
                                        Modifier.floatingToolbarVerticalNestedScroll(
                                            expanded = expandedToolbar,
                                            onExpand = { expandedToolbar = true },
                                            onCollapse = { expandedToolbar = false }
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .nestedScroll(scrollBehavior.nestedScrollConnection)
                        ) {
                            if(!vm.loaded) {
                                item { ShoppingListGroupHeaderListItemPlaceholder() }
                                item { ShoppingListEntryListItemPlaceholder() }
                                item { ShoppingListEntryListItemPlaceholder() }
                                item { ShoppingListEntryListItemPlaceholder() }
                                item { ShoppingListEntryListItemPlaceholder() }
                                item {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                                    )
                                }
                                item { ShoppingListGroupHeaderListItemPlaceholder() }
                                item { ShoppingListEntryListItemPlaceholder() }
                                item { ShoppingListEntryListItemPlaceholder() }
                                item { ShoppingListEntryListItemPlaceholder() }
                            } else {
                                items(vm.items.size, key = { vm.items[it].key }) {
                                    when(val item = vm.items[it]) {
                                        is GroupHeaderShoppingListItemModel -> {
                                            ShoppingListGroupHeaderListItem(
                                                label = { item.label }
                                            )
                                        }

                                        is GroupedFoodShoppingListItemModel -> {
                                            ShoppingListEntryListItem(
                                                food = item.food,
                                                entries = item.entries,
                                                showFractionalValues = ingredientsShowFractionalValues.value,
                                                selectionState = selectionModeState,
                                                onClick = {
                                                    shoppingListEntryDetailsBottomSheetState.open(
                                                        item.entries
                                                    )
                                                },
                                                onClickExpand = {
                                                    shoppingListEntryDetailsBottomSheetState.open(
                                                        item.entries
                                                    )
                                                }
                                            )
                                        }

                                        is GroupDividerShoppingListItemModel -> {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(
                                                    start = 16.dp,
                                                    end = 16.dp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Box(
                Modifier.fillMaxWidth()
                    .offset(y = 58.dp)
                    .offset(y = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContainedLoadingIndicator(
                    visible = vm.shoppingListEntriesFetchRequest.state == TandoorRequestStateState.LOADING
                )
            }

            VerticalFloatingToolbar(
                modifier = Modifier.align(Alignment.BottomEnd)
                    .offset(x = -ScreenOffset, y = -ScreenOffset),
                expanded = expandedToolbar,
                content = {
                    IconButton(
                        onClick = {
                            p.vm.navHostController?.navigate("shopping/shoppingMode")
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                        }
                    ) {
                        Icon(
                            Icons.Rounded.Storefront,
                            stringResource(Res.string.action_start_shopping_mode)
                        )
                    }

                    IconButton(
                        onClick = {
                            shoppingListEntriesClearDialogState.open()
                        }
                    ) {
                        IconWithState(
                            imageVector = Icons.Rounded.CleaningServices,
                            contentDescription = stringResource(Res.string.action_clear),
                            state = entriesClearRequestState.state.toIconWithState()
                        )
                    }
                },
                floatingActionButton = {
                    FloatingToolbarDefaults.StandardFloatingActionButton(
                        onClick = {
                            shoppingListEntryCreationDialogState.open()
                        }
                    ) {
                        Icon(Icons.Rounded.Add, stringResource(Res.string.action_add))
                    }
                }
            )
        }
    }

    client?.let {
        ShoppingListEntriesClearDialog(
            state = shoppingListEntriesClearDialogState,
            onClear = { onlyDoneEntries ->
                coroutineScope.launch {
                    entriesClearRequestState.wrapRequest {
                        val entries = client.shopping.fetchAll().toMutableList()
                        if (onlyDoneEntries) entries.removeIf { !it.checked }

                        client.shopping.delete(entries)
                        repeat(entries.size) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            delay(25)
                        }

                        vm.renderItems()
                        vm.update()
                    }
                }
            }
        )

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

        ShoppingListEntryCreationDialog(
            client = it,
            state = shoppingListEntryCreationDialogState,
            onUpdate = { entry ->
                coroutineScope.launch {
                    vm.entries.add(entry)
                    vm.renderItems()
                }
            }
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