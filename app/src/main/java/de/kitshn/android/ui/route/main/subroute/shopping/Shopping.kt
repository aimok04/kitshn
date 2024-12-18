package de.kitshn.android.ui.route.main.subroute.shopping

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.RemoveShoppingCart
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.TandoorRequestState
import de.kitshn.android.api.tandoor.TandoorRequestStateState
import de.kitshn.android.api.tandoor.model.TandoorFood
import de.kitshn.android.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.android.api.tandoor.model.shopping.TandoorShoppingListEntryRecipeMealplan
import de.kitshn.android.api.tandoor.rememberTandoorRequestState
import de.kitshn.android.formatAmount
import de.kitshn.android.parseUtcTandoorDate
import de.kitshn.android.toHumanReadableDateLabel
import de.kitshn.android.ui.TandoorRequestErrorHandler
import de.kitshn.android.ui.component.alert.FullSizeAlertPane
import de.kitshn.android.ui.component.icons.IconWithState
import de.kitshn.android.ui.component.input.OutlinedNumberField
import de.kitshn.android.ui.dialog.mealplan.MealPlanCreationAndEditDialog
import de.kitshn.android.ui.dialog.mealplan.MealPlanDetailsDialog
import de.kitshn.android.ui.dialog.mealplan.rememberMealPlanDetailsDialogState
import de.kitshn.android.ui.dialog.mealplan.rememberMealPlanEditDialogState
import de.kitshn.android.ui.dialog.recipe.RecipeLinkDialog
import de.kitshn.android.ui.dialog.recipe.rememberRecipeLinkDialogState
import de.kitshn.android.ui.route.RouteParameters
import de.kitshn.android.ui.view.ViewParameters
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMainSubrouteShopping(
    p: RouteParameters
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val ingredientsShowFractionalValues =
        p.vm.settings.getIngredientsShowFractionalValues.collectAsState(initial = true)

    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val shoppingListAddEntryRequest = rememberTandoorRequestState()

    var loaded by remember { mutableStateOf(false) }

    val shoppingListEntriesFetchRequest = rememberTandoorRequestState()
    val shoppingListEntriesCheckRequest = rememberTandoorRequestState()

    val client = p.vm.tandoorClient
    var blockUpdate by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while(true) {
            if(client != null) {
                if(blockUpdate) {
                    blockUpdate = false
                } else {
                    shoppingListEntriesFetchRequest.wrapRequest {
                        val items = client.shopping.fetch()
                        if(blockUpdate) {
                            blockUpdate = false
                            return@wrapRequest
                        }

                        client.container.shoppingListEntries.clear()
                        client.container.shoppingListEntries.addAll(items)
                        client.container.shoppingListEntries.reverse()

                        loaded = true
                    }
                }
            }

            delay(5000)
        }
    }

    val mealPlanDetailsDialogState = rememberMealPlanDetailsDialogState()
    val mealPlanEditDialogState =
        rememberMealPlanEditDialogState(key = "RouteMainSubrouteShopping/mealPlanEditDialogState")
    val recipeLinkDialogState = rememberRecipeLinkDialogState()

    val foods = remember { mutableStateListOf<TandoorFood>() }
    val foodMap = remember { mutableStateMapOf<Int, MutableList<TandoorShoppingListEntry>>() }

    LaunchedEffect(client?.container?.shoppingListEntries?.toList()) {
        foods.clear()
        foodMap.clear()

        client?.container?.shoppingListEntries?.forEach {
            if(it.checked) return@forEach

            if(!foodMap.contains(it.food.id)) {
                foods.add(it.food)
                foodMap[it.food.id] = mutableListOf()
            }

            foodMap[it.food.id]?.add(it)
        }
    }

    var amount by remember { mutableStateOf<Int?>(null) }
    var unit by remember { mutableStateOf<String?>(null) }
    var food by remember { mutableStateOf<String?>(null) }

    fun add() {
        coroutineScope.launch {
            shoppingListAddEntryRequest.wrapRequest {
                client!!.shopping.add(amount?.toDouble(), food, unit)
            }

            focusManager.clearFocus()

            amount = null
            unit = null
            food = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.navigation_shopping)) },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    Row(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 16.dp, bottom = 10.dp)
                    ) {
                        OutlinedNumberField(
                            modifier = Modifier.weight(0.25f),

                            placeholder = { Text(text = stringResource(R.string.shopping_list_entry_create_placeholder_amount)) },

                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Next) }
                            ),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),

                            value = amount,
                            onValueChange = {
                                amount = it
                            }
                        )

                        Spacer(Modifier.width(8.dp))

                        OutlinedTextField(
                            modifier = Modifier.weight(0.25f),

                            placeholder = { Text(text = stringResource(R.string.shopping_list_entry_create_placeholder_unit)) },

                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Next) }
                            ),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),

                            singleLine = true,
                            maxLines = 1,

                            value = unit ?: "",
                            onValueChange = {
                                unit = it
                                if(unit?.isBlank() == true) unit = null
                            }
                        )

                        Spacer(Modifier.width(8.dp))

                        OutlinedTextField(
                            modifier = Modifier.weight(0.5f),

                            placeholder = { Text(text = stringResource(R.string.shopping_list_entry_create_placeholder_food)) },

                            keyboardActions = KeyboardActions(
                                onGo = { add() }
                            ),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Go
                            ),

                            singleLine = true,
                            maxLines = 1,

                            value = food ?: "",
                            onValueChange = {
                                food = it
                                if(food?.isBlank() == true) food = null
                            }
                        )
                    }

                },
                floatingActionButton = {
                    FloatingActionButton(
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                        onClick = {
                            add()
                        }
                    ) {
                        IconWithState(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(id = R.string.action_add),
                            state = shoppingListAddEntryRequest.state.toIconWithState()
                        )
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
                        if(shoppingListEntriesFetchRequest.state == TandoorRequestStateState.LOADING) 1f else 0f
                    )
            )

            if(foods.isEmpty() && loaded) {
                FullSizeAlertPane(
                    imageVector = Icons.Rounded.RemoveShoppingCart,
                    contentDescription = stringResource(R.string.shopping_list_empty),
                    text = stringResource(R.string.shopping_list_empty)
                )
            } else {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                ) {
                    items(foods.size, key = { foods[it].id }) { foodIndex ->
                        val currentFood = foods[foodIndex]
                        val currentEntries = foodMap[currentFood.id]

                        val mealplans =
                            remember { mutableStateListOf<TandoorShoppingListEntryRecipeMealplan>() }

                        val leadingLabels = remember { mutableStateListOf<String>() }

                        LaunchedEffect(currentEntries) {
                            mealplans.clear()
                            mealplans.addAll(
                                currentEntries!!.filter { it.recipe_mealplan != null }
                                    .map { it.recipe_mealplan!! }
                            )

                            leadingLabels.clear()
                            leadingLabels.addAll(
                                currentEntries.filter { it.amount != 0.0 || it.unit != null }
                                    .groupBy { it.unit?.id ?: -100 }
                                    .values
                                    .map { entryList ->
                                        val sharedAmount = entryList.sumOf { it.amount }
                                        val sharedUnit = entryList[0].unit

                                        sharedAmount.formatAmount(ingredientsShowFractionalValues.value) + " " + (sharedUnit?.name?.let { "$it " }
                                            ?: "")
                                    }
                            )
                        }

                        ListItem(
                            colors = ListItemDefaults.colors(
                                supportingColor = MaterialTheme.colorScheme.primary
                            ),
                            supportingContent = if(mealplans.size > 0) {
                                {
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        mealplans.forEach { mealplan ->
                                            Text(
                                                modifier = Modifier.clickable {
                                                    if(client == null) return@clickable

                                                    coroutineScope.launch {
                                                        TandoorRequestState().wrapRequest {
                                                            if(mealplan.mealplan != null) {
                                                                mealPlanDetailsDialogState.open(
                                                                    client.mealPlan.get(mealplan.mealplan)
                                                                )
                                                            } else {
                                                                val recipe = client.recipe.get(
                                                                    mealplan.recipe,
                                                                    true
                                                                )
                                                                recipeLinkDialogState.open(
                                                                    recipe.toOverview()
                                                                )
                                                            }
                                                        }
                                                    }
                                                },
                                                text = if(mealplan.mealplan != null) {
                                                    mealplan.mealplan_from_date?.parseUtcTandoorDate()
                                                        ?.toHumanReadableDateLabel() + " — "
                                                } else {
                                                    ""
                                                } + mealplan.name
                                            )
                                        }
                                    }
                                }
                            } else {
                                null
                            },
                            leadingContent = if(leadingLabels.size > 0) {
                                {
                                    Column {
                                        leadingLabels.forEach {
                                            Text(
                                                fontWeight = FontWeight.SemiBold,
                                                text = it
                                            )
                                        }
                                    }
                                }
                            } else {
                                null
                            },
                            headlineContent = {
                                Text(text = currentFood.name)
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = {
                                        blockUpdate = true

                                        coroutineScope.launch {
                                            shoppingListEntriesCheckRequest.wrapRequest {
                                                currentEntries?.forEach {
                                                    it.check()
                                                }
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Rounded.Check,
                                        stringResource(R.string.action_mark_as_done)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if(p.vm.tandoorClient != null) {
        MealPlanCreationAndEditDialog(
            client = p.vm.tandoorClient!!,
            editState = mealPlanEditDialogState
        ) { }
    }

    MealPlanDetailsDialog(
        p = ViewParameters(
            vm = p.vm,
            back = null
        ),
        state = mealPlanDetailsDialogState,
        onUpdateList = { }
    ) {
        mealPlanEditDialogState.open(it)
    }

    RecipeLinkDialog(
        p = ViewParameters(
            vm = p.vm,
            back = null
        ),
        state = recipeLinkDialogState
    )

    TandoorRequestErrorHandler(shoppingListEntriesFetchRequest)
    TandoorRequestErrorHandler(shoppingListEntriesCheckRequest)
}