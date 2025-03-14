package de.kitshn.ui.route.main.subroute.shopping

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.RemoveShoppingCart
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.cache.ShoppingListEntriesCache
import de.kitshn.cache.ShoppingListEntryOfflineActions
import de.kitshn.cache.ShoppingSupermarketCache
import de.kitshn.model.route.GroupDividerShoppingListItemModel
import de.kitshn.model.route.GroupHeaderShoppingListItemModel
import de.kitshn.model.route.GroupedFoodShoppingListItemModel
import de.kitshn.model.route.ShoppingViewModel
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.LoadingGradientWrapper
import de.kitshn.ui.component.alert.FullSizeAlertPane
import de.kitshn.ui.component.icons.IconWithState
import de.kitshn.ui.component.model.shopping.ShoppingListEntryListItem
import de.kitshn.ui.component.model.shopping.ShoppingListEntryListItemPlaceholder
import de.kitshn.ui.component.model.shopping.ShoppingListGroupHeaderListItem
import de.kitshn.ui.component.model.shopping.ShoppingListGroupHeaderListItemPlaceholder
import de.kitshn.ui.component.shopping.AdditionalShoppingSettingsChipRow
import de.kitshn.ui.dialog.mealplan.MealPlanDetailsDialog
import de.kitshn.ui.dialog.mealplan.rememberMealPlanDetailsDialogState
import de.kitshn.ui.dialog.recipe.RecipeLinkDialog
import de.kitshn.ui.dialog.recipe.rememberRecipeLinkDialogState
import de.kitshn.ui.dialog.shopping.ShoppingListEntryCreationDialog
import de.kitshn.ui.dialog.shopping.ShoppingListEntryDetailsBottomSheet
import de.kitshn.ui.dialog.shopping.rememberShoppingListEntryCreationDialogState
import de.kitshn.ui.dialog.shopping.rememberShoppingListEntryDetailsBottomSheetState
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.selectionMode.component.SelectionModeTopAppBar
import de.kitshn.ui.selectionMode.rememberSelectionModeState
import de.kitshn.ui.state.ErrorLoadingSuccessState
import de.kitshn.ui.view.ViewParameters
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_add
import kitshn.composeapp.generated.resources.action_delete
import kitshn.composeapp.generated.resources.action_mark_as_done
import kitshn.composeapp.generated.resources.action_start_shopping_mode
import kitshn.composeapp.generated.resources.common_shopping_mode
import kitshn.composeapp.generated.resources.navigation_shopping
import kitshn.composeapp.generated.resources.shopping_list_empty
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
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

    val additionalShoppingSettingsChipRowState = p.vm.uiState.additionalShoppingSettingsChipRowState

    val mealPlanDetailsDialogState = rememberMealPlanDetailsDialogState()
    val recipeLinkDialogState = rememberRecipeLinkDialogState()

    val shoppingListEntryCreationDialogState = rememberShoppingListEntryCreationDialogState()
    val shoppingListEntryDetailsBottomSheetState =
        rememberShoppingListEntryDetailsBottomSheetState()

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
                    TopAppBar(
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
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    FilledTonalButton(
                        modifier = Modifier
                            .padding(start = 12.dp, bottom = 4.dp),
                        onClick = {
                            p.vm.navHostController?.navigate("shopping/shoppingMode")
                        }
                    ) {
                        Icon(
                            Icons.Rounded.Storefront,
                            stringResource(Res.string.action_start_shopping_mode)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(stringResource(Res.string.common_shopping_mode))
                    }
                },
                floatingActionButton = {
                    if(!p.vm.uiState.offlineState.isOffline) FloatingActionButton(
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                        onClick = {
                            shoppingListEntryCreationDialogState.open()
                        }
                    ) {
                        Icon(Icons.Rounded.Add, stringResource(Res.string.action_add))
                    }
                }
            )
        }
    ) { pv ->
        Column(
            Modifier.padding(pv)
        ) {
            LinearProgressIndicator(
                Modifier
                    .fillMaxWidth()
                    .alpha(
                        if(vm.shoppingListEntriesFetchRequest.state == TandoorRequestStateState.LOADING) 1f else 0f
                    )
            )

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
                }
            },
            onDelete = { entries ->
                if(p.vm.uiState.offlineState.isOffline) {
                    vm.executeOfflineAction(entries, ShoppingListEntryOfflineActions.DELETE)
                    return@ShoppingListEntryDetailsBottomSheet
                }

                coroutineScope.launch {
                    actionRequestState.wrapRequest {
                        entries.forEach { entry -> entry.delete() }
                        vm.renderItems()
                    }
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