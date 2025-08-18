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
import de.kitshn.withLeadingZeros
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_ungrouped
import kitshn.composeapp.generated.resources.shopping_list_items_done
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
    val shoppingListEntriesFetchRequest = TandoorRequestState()

    var loaded by mutableStateOf(false)

    init {
        // add cached items to entries list
        entries.addAll(
            cache.retrieve()
                ?: listOf()
        )

        entries.forEach {
            it.client = p.vm.tandoorClient
        }

        // display cached items after waiting 3 seconds
        viewModelScope.launch {
            if(!p.vm.uiState.offlineState.isOffline)
                delay(3000)

            renderItems()
            loaded = true
        }
    }

    suspend fun update() {
        while(client == null) {
            delay(50)
        }

        // don't update if offline
        if(p.vm.uiState.offlineState.isOffline) return

        // don't update when app is in background
        if(!p.vm.uiState.isInForeground) return

        shoppingListEntriesFetchRequest.wrapRequest {
            // improve loading UI and progress bar
            delay(500)

            // process offline actions
            val offlineActions = cache.retrieveOfflineActions()
            offlineActions.forEach {
                try {
                    when(it.value) {
                        ShoppingListEntryOfflineActions.CHECK -> client.shopping.check(setOf(it.key))
                        ShoppingListEntryOfflineActions.UNCHECK -> client.shopping.uncheck(setOf(it.key))
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

            val oldEntriesMap = mutableMapOf<Int, TandoorShoppingListEntry>()
            entries.forEach { oldEntriesMap[it.id] = it }

            // keep old items if JSON is matching
            val newEntries = client.shopping.fetchAll().map {
                if(!oldEntriesMap.containsKey(it.id)) {
                    it
                } else if(json.encodeToString(oldEntriesMap[it.id]) != json.encodeToString(it)) {
                    it
                } else {
                    oldEntriesMap[it.id]!!
                }
            }

            // remove all items
            entries.clear()

            // prevent duplicate adds
            val addedIds = mutableListOf<Int>()
            addedIds.addAll(entries.map { it.id })

            // add all items
            newEntries.forEach {
                if(addedIds.contains(it.id)) return@forEach
                entries.add(it)
            }

            // cache entries for offline use
            cache.update(entries)

            renderItems()
            loaded = true
        }
    }

    suspend fun renderItems() {
        items.clear()
        if(entries.isEmpty()) return

        val supermarketCategoryIdToOrder =
            additionalShoppingSettingsChipRowState.supermarket?.category_to_supermarket?.associate {
                Pair(it.category.id, it.order)
            }

        val maxOrderInt = supermarketCategoryIdToOrder?.entries?.maxOfOrNull { it.value } ?: 0
        val maxOrderIntDigitCount = maxOrderInt.toString().length

        val entries = this.entries.filterNot { it._destroyed }
            .toMutableList()

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
                            label = getString(Res.string.shopping_list_items_done)
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
                            supermarketCategoryIdToOrder[it.food.supermarket_category!!.id]
                                ?.withLeadingZeros(length = maxOrderIntDigitCount)
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
                                    label = getString(Res.string.shopping_list_items_done)
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
                        it.list_recipe_data?.let { data ->
                            if (data.recipe == null || data.recipe_data == null) return@let
                            groupIdMap[data.recipe] = data.recipe_data.name
                        }
                    }
                    .sortedBy {
                        if(moveDoneToBottom && it.checked) {
                            "\uffff\uFFFF\uFFFF" // last character in lexicographic order
                        } else {
                            it.list_recipe_data?.recipe_data?.name ?: "0"
                        }
                    }
                    .groupBy {
                        if(moveDoneToBottom && it.checked) {
                            Int.MAX_VALUE
                        } else {
                            it.list_recipe_data?.recipe ?: -1
                        }
                    }
                    .forEach {
                        if(it.key == Int.MAX_VALUE) {
                            items.add(
                                GroupHeaderShoppingListItemModel(
                                    id = it.key,
                                    label = getString(Res.string.shopping_list_items_done)
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
                                    label = getString(Res.string.shopping_list_items_done)
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
            .sortedBy { it.food.name.lowercase() }
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
                ShoppingListEntryOfflineActions.CHECK -> {
                    it.checked = true
                    it._checked = true
                }

                ShoppingListEntryOfflineActions.UNCHECK -> {
                    it.checked = false
                    it._checked = false
                }

                ShoppingListEntryOfflineActions.DELETE -> {
                    it.destroyed = true
                    it._destroyed = true
                }
            }
        }

        cache.update(this.entries)

        viewModelScope.launch {
            renderItems()
        }
    }

}
