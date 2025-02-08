package de.kitshn.model.route

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.kitshn.api.tandoor.TandoorRequestState
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.api.tandoor.model.TandoorFood
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarketCategory
import de.kitshn.cache.ShoppingListEntriesCache
import de.kitshn.cache.ShoppingListEntryOfflineActions
import de.kitshn.json
import de.kitshn.removeIf
import de.kitshn.ui.component.shopping.AdditionalShoppingSettingsChipRowState
import de.kitshn.ui.component.shopping.chips.GroupingOptions
import de.kitshn.ui.route.RouteParameters
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_ungrouped
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

open class ShoppingListItemModel(
    val key: String
)

class GroupHeaderShoppingListItemModel(
    val id: Int,
    val label: String
) : ShoppingListItemModel(
    "group-$id"
)

class GroupedFoodShoppingListItemModel(
    val groupId: Int,
    val food: TandoorFood,
    val entries: List<TandoorShoppingListEntry>
) : ShoppingListItemModel(
    "group-$groupId-food${food.id}"
)

class GroupDividerShoppingListItemModel(
    groupId: Int
) : ShoppingListItemModel(
    "group-$groupId-divider"
)

class ShoppingViewModel(
    val p: RouteParameters,
    val additionalShoppingSettingsChipRowState: AdditionalShoppingSettingsChipRowState,
    val cache: ShoppingListEntriesCache,
    val moveDoneToBottom: Boolean = false
) : ViewModel() {

    val client = p.vm.tandoorClient

    val entries = mutableStateListOf<TandoorShoppingListEntry>()

    val items = mutableStateListOf<ShoppingListItemModel>()
    private var previous: String? = null

    val shoppingListEntriesFetchRequest = TandoorRequestState()

    var loaded by mutableStateOf(false)

    private var blockUI by mutableStateOf(false)
    fun blockUI() {
        blockUI = true
    }

    init {
        // add cached items to entries list
        entries.addAll(
            cache.retrieve()
                ?: listOf()
        )

        // display cached items after waiting 3 seconds
        viewModelScope.launch {
            delay(3000)
            if(entries.isEmpty()) return@launch

            renderItems(delay = false)
            loaded = true
        }
    }

    suspend fun update() {
        while(client == null) {
            delay(50)
        }

        if(blockUI) {
            blockUI = false
            return
        }

        // don't update if offline
        if(p.vm.uiState.offlineState.isOffline) return

        shoppingListEntriesFetchRequest.wrapRequest {
            // process offline actions
            val offlineActions = cache.retrieveOfflineActions()
            offlineActions.forEach {
                try {
                    when(it.value) {
                        ShoppingListEntryOfflineActions.CHECK -> client.shopping.check(listOf(it.key))
                        ShoppingListEntryOfflineActions.DELETE -> client.shopping.delete(it.key)
                    }

                    // delete offline action on success
                    cache.resetOfflineAction(it.key)
                } catch(e: Throwable) {
                    if(e is TandoorRequestsError) {
                        if(e.response == null) return@forEach

                        // delete offline action when server sent response
                        cache.resetOfflineAction(it.key)
                    }
                }
            }

            val mEntries = client.shopping.fetch()

            val dataStr = json.encodeToString(mEntries)
            if(previous == dataStr) return@wrapRequest

            // remove all items that are not checked (checked items are removed server-side)
            entries.removeIf { !it.checked }

            // prevent duplicate adds
            val addedIds = mutableListOf<Int>()
            addedIds.addAll(entries.map { it.id })

            // add all items
            mEntries.forEach {
                if(addedIds.contains(it.id)) return@forEach
                entries.add(it)
            }

            // cache entries for offline use
            cache.update(entries)

            if(blockUI) {
                blockUI = false
                return@wrapRequest
            }

            previous = dataStr

            renderItems(delay = false)
            loaded = true
        }
    }

    suspend fun renderItems(
        delay: Boolean = true
    ) {
        if(delay) delay(100)

        items.clear()
        if(entries.isEmpty()) return

        entries.removeIf { it._destroyed }

        val supermarketCategoryIdToOrder =
            additionalShoppingSettingsChipRowState.supermarket?.category_to_supermarket?.associate {
                Pair(it.category.id, it.order)
            }

        val entries = this.entries.toMutableList()

        // filter out unavailable categories for supermarket
        if(supermarketCategoryIdToOrder != null) entries.removeIf {
            if(it.food.supermarket_category == null) {
                false
            } else {
                !supermarketCategoryIdToOrder.containsKey(it.food.supermarket_category?.id)
            }
        }

        // group by selected variable
        when(additionalShoppingSettingsChipRowState.grouping) {
            GroupingOptions.NONE -> {
                if(moveDoneToBottom) {
                    combineEntriesByFood(
                        groupId = 0,
                        entries = entries.filter { !it.checked }
                    )

                    if(entries.firstOrNull { it.checked } == null) return

                    items.add(
                        GroupHeaderShoppingListItemModel(
                            id = Int.MAX_VALUE,
                            label = "Done"
                        )
                    )

                    combineEntriesByFood(
                        groupId = Int.MAX_VALUE,
                        entries = entries.filter { it.checked }
                    )
                } else {
                    combineEntriesByFood(
                        groupId = 0,
                        entries = entries
                    )
                }
            }

            GroupingOptions.BY_CATEGORY -> {
                val groupIdMap = mutableStateMapOf<Int, TandoorSupermarketCategory>()
                entries
                    .onEach {
                        it.food.supermarket_category?.let { category ->
                            groupIdMap[category.id ?: -1] = category
                        }
                    }
                    .sortedBy {
                        if(moveDoneToBottom && it.checked) {
                            "\uffff\uFFFF\uFFFF" // last character in lexicographic order
                        } else if(it.food.supermarket_category == null) {
                            "!!!" // first character in lexicographic order
                        } else if(supermarketCategoryIdToOrder != null) {
                            (supermarketCategoryIdToOrder[it.food.supermarket_category!!.id]
                                ?: 0).toString()
                        } else {
                            it.food.supermarket_category!!.name
                        }
                    }
                    .groupBy {
                        if(moveDoneToBottom && it.checked) {
                            Int.MAX_VALUE
                        } else {
                            it.food.supermarket_category?.id ?: -1
                        }
                    }
                    .forEach {
                        if(it.key == Int.MAX_VALUE) {
                            items.add(
                                GroupHeaderShoppingListItemModel(
                                    id = it.key,
                                    label = "Done"
                                )
                            )

                            combineEntriesByFood(it.key, it.value)
                        } else {
                            val category = groupIdMap[it.key]

                            items.add(
                                GroupHeaderShoppingListItemModel(
                                    id = it.key,
                                    label = category?.name ?: getString(Res.string.common_ungrouped)
                                )
                            )

                            combineEntriesByFood(it.key, it.value)
                        }
                    }
            }

            GroupingOptions.BY_RECIPE -> {
                val groupIdMap = mutableStateMapOf<Int, String>()
                entries
                    .onEach {
                        it.recipe_mealplan?.let { recipeMealplan ->
                            groupIdMap[recipeMealplan.recipe] = recipeMealplan.recipe_name
                        }
                    }
                    .sortedBy {
                        if(moveDoneToBottom && it.checked) {
                            "\uffff\uFFFF\uFFFF" // last character in lexicographic order
                        } else {
                            it.recipe_mealplan?.recipe_name ?: "0"
                        }
                    }
                    .groupBy {
                        if(moveDoneToBottom && it.checked) {
                            Int.MAX_VALUE
                        } else {
                            it.recipe_mealplan?.recipe ?: -1
                        }
                    }
                    .forEach {
                        if(it.key == Int.MAX_VALUE) {
                            items.add(
                                GroupHeaderShoppingListItemModel(
                                    id = it.key,
                                    label = "Done"
                                )
                            )

                            combineEntriesByFood(it.key, it.value)
                        } else {
                            items.add(
                                GroupHeaderShoppingListItemModel(
                                    id = it.key,
                                    label = groupIdMap[it.key]
                                        ?: getString(Res.string.common_ungrouped)
                                )
                            )

                            combineEntriesByFood(it.key, it.value)
                        }
                    }
            }

            GroupingOptions.BY_CREATOR -> {
                val groupIdMap = mutableStateMapOf<Int, String>()
                entries
                    .onEach {
                        it.created_by.let { createdBy ->
                            groupIdMap[createdBy.id] = createdBy.display_name
                        }
                    }
                    .sortedBy {
                        if(moveDoneToBottom && it.checked) {
                            "\uffff\uFFFF\uFFFF" // last character in lexicographic order
                        } else {
                            it.created_by.display_name
                        }
                    }
                    .groupBy {
                        if(moveDoneToBottom && it.checked) {
                            Int.MAX_VALUE
                        } else {
                            it.created_by.id
                        }
                    }
                    .forEach {
                        if(it.key == Int.MAX_VALUE) {
                            items.add(
                                GroupHeaderShoppingListItemModel(
                                    id = it.key,
                                    label = "Done"
                                )
                            )

                            combineEntriesByFood(it.key, it.value)
                        } else {
                            items.add(
                                GroupHeaderShoppingListItemModel(
                                    id = it.key,
                                    label = groupIdMap[it.key]
                                        ?: getString(Res.string.common_ungrouped)
                                )
                            )

                            combineEntriesByFood(it.key, it.value)
                        }
                    }
            }
        }

        if(items.lastOrNull() is GroupDividerShoppingListItemModel)
            items.removeLastOrNull()
    }

    private fun combineEntriesByFood(
        groupId: Int,
        entries: List<TandoorShoppingListEntry>
    ) {
        val foodIdMap = mutableMapOf<Int, TandoorFood>()
        entries
            .onEach { foodIdMap[it.food.id] = it.food }
            .sortedBy { it.food.name }
            .groupBy { it.food.id }
            .forEach { entry ->
                foodIdMap[entry.key]?.let { food ->
                    items.add(
                        GroupedFoodShoppingListItemModel(
                            groupId = groupId,
                            food = food,
                            entries = entry.value
                        )
                    )
                }
            }

        items.add(
            GroupDividerShoppingListItemModel(
                groupId = groupId
            )
        )
    }

    fun executeOfflineAction(
        entries: List<TandoorShoppingListEntry>,
        action: ShoppingListEntryOfflineActions
    ) {
        entries.forEach {
            cache.setOfflineAction(it.id, action)

            when(action) {
                ShoppingListEntryOfflineActions.CHECK -> it.checked = true
                ShoppingListEntryOfflineActions.DELETE -> it._destroyed = true
            }
        }

        cache.update(this.entries)

        viewModelScope.launch {
            renderItems()
        }
    }

}