package de.kitshn.repo

import co.touchlab.kermit.Logger
import de.kitshn.AppDatabase
import de.kitshn.api.tandoor.model.TandoorUnit
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingList
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntryCreatedBy
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntryFood
import de.kitshn.db.entity.ShoppingCreatePayload
import de.kitshn.db.entity.ShoppingItemEntity
import de.kitshn.db.entity.ShoppingListEntryOfflineActions
import de.kitshn.db.entity.ShoppingTransactionEntity
import de.kitshn.db.entity.ShoppingUpdatePayload
import de.kitshn.db.entity.toEntity
import de.kitshn.db.entity.toModel
import de.kitshn.json
import de.kitshn.session.TandoorSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration

data class TandoorShoppingListEntryCreationRequest(
    val foodName: String,
    val amount: Double,
    val unitName: String? = null,
    val shoppingLists: List<TandoorShoppingList> = listOf(),
    val mealPlanId: Int? = null,
    val listRecipeId: Long? = null,
    val order: Long? = null,
    val checked: Boolean = false,
)

class ShoppingRepo(
    db: AppDatabase,
    private val unitRepo: UnitRepo,
    private val session: TandoorSession,
    private val scope: CoroutineScope,
    periodicInterval: Duration? = null,
) : SyncableRepo(periodicInterval) {
    private val dao = db.shoppingDao()
    private val syncPendingMutex = Mutex()

    fun observe(): Flow<List<TandoorShoppingListEntry>> = 
        dao.getAllAsFlow().map { entries -> entries.mapNotNull { it.toModel() } }

    override suspend fun performSync() {
        val client = session.client ?: return
        try {
            val recentlySyncedIds = syncPending()

            val remoteItems = client.shopping.fetchAll()
            unitRepo.upsertAll(remoteItems.mapNotNull { it.unit })

            // Protect items with pending transactions OR items that were just synced.
            // This prevents items created/updated offline (which might be checked) from being
            // deleted if they don't appear in the (unchecked-only) fetchAll() list.
            val pendingIds = dao.getPendingTransactions().map { it.entryId }
            val protectedIds = (pendingIds + recentlySyncedIds).distinct()
            dao.syncRemoteItems(remoteItems.map { it.toEntity() }, protectedIds)
        } catch (e: Exception) {
            Logger.e(e, tag = "ShoppingRepository") { "Failed to refresh shopping items" }
        }
    }

    suspend fun toggleCheck(
        entryId: Int,
        checked: Boolean,
    ) = toggleCheckBulk(listOf(entryId), checked)

    suspend fun toggleCheckBulk(
        entryIds: Collection<Int>,
        checked: Boolean,
    ) {
        if (entryIds.isEmpty()) return
        val action =
            if (checked) {
                ShoppingListEntryOfflineActions.CHECK
            } else {
                ShoppingListEntryOfflineActions.UNCHECK
                val remoteItems = client.shopping.fetchAll()
                withContext(Dispatchers.IO) {
                    dao.syncRemoteItems(remoteItems.map { it.toEntity() })
                }
            } catch (e: Exception) {
                Logger.e(e, tag = "ShoppingRepository") { "Failed to refresh shopping items" }
            }

        entryIds.forEach { id ->
            dao.updateChecked(id, checked)
            dao.insertTransaction(
                ShoppingTransactionEntity(entryId = id, action = action),
            )
        }

        scheduleSyncPending()
    }

    suspend fun create(
        foodName: String,
        amount: Double,
        unitName: String? = null,
        shoppingLists: List<TandoorShoppingList> = listOf(),
        mealPlanId: Int? = null,
        listRecipeId: Long? = null,
        order: Long? = null,
        checked: Boolean = false,
    ): TandoorShoppingListEntry? {

        val localId = dao.nextLocalId()
        dao.upsertAll(
            listOf(
                ShoppingItemEntity(
                    id = localId,
                    list_recipe = listRecipeId,
                    shopping_lists = shoppingLists,
                    food = TandoorShoppingListEntryFood(
                        id = -1,
                        name = foodName,
                    ),
                    unit = TandoorUnit(
                        id = -1,
                        name = unitName.orEmpty()
                    ),
                    amount = amount,
                    order = order ?: 0L,
                    checked = checked,
                    created_by = OFFLINE_CREATED_BY,
                ),
            ),
        )
        val payload =
            json.encodeToString(
                ShoppingCreatePayload(
                    foodName = foodName,
                    amount = amount,
                    unitName = unitName,
                    shoppingLists = shoppingLists,
                    mealPlanId = mealPlanId,
                    listRecipeId = listRecipeId,
                    order = order,
                    checked = checked,
                ),
            )
        dao.insertTransaction(
            ShoppingTransactionEntity(
                entryId = localId,
                action = ShoppingListEntryOfflineActions.CREATE,
                payload = payload,
            ),
        )

        scheduleSyncPending()
        return dao.getWithRelationsById(localId)?.toModel()
    }

    suspend fun createBulk(entries: List<TandoorShoppingListEntryCreationRequest>) {
        entries.forEach { req ->
            create(
                foodName = req.foodName,
                amount = req.amount,
                unitName = req.unitName,
                shoppingLists = req.shoppingLists,
                mealPlanId = req.mealPlanId,
                listRecipeId = req.listRecipeId,
                order = req.order,
                checked = req.checked,
            )
        }
    }

    suspend fun delete(entryId: Int) = deleteBulk(listOf(entryId))

    suspend fun deleteBulk(entryIds: List<Int>) {
        if (entryIds.isEmpty()) return
        entryIds.forEach { entryId ->
            dao.delete(entryId)
            dao.insertTransaction(
                ShoppingTransactionEntity(
                    entryId = entryId,
                    action = ShoppingListEntryOfflineActions.DELETE,
                ),
            )
        }

        scheduleSyncPending()
    }

    suspend fun deleteAll(onlyChecked: Boolean = false) {
        val snapshot = observe().first()
        val ids =
            snapshot
                .filter { if (onlyChecked) it.checked else true }
                .map { it.id }
        deleteBulk(ids)
    }

    suspend fun fetchLists(): List<TandoorShoppingList> {
        val client = session.client ?: return emptyList()
        return try {
            client.shopping.fetchAllLists()
        } catch (e: Exception) {
            Logger.e(e, tag = "ShoppingRepository") { "Failed to fetch shopping lists" }
            emptyList()
        }
    }

    suspend fun updatePartial(
        entryId: Int,
        amount: Double? = null,
        unitName: String? = null,
        clearUnit: Boolean = false,
    ): TandoorShoppingListEntry? {

        if (amount != null) dao.updateAmount(entryId, amount)
        dao.insertTransaction(
            ShoppingTransactionEntity(
                entryId = entryId,
                action = ShoppingListEntryOfflineActions.UPDATE,
                payload =
                    json.encodeToString(
                        ShoppingUpdatePayload(
                            amount = amount,
                            unitName = unitName,
                            clearUnit = clearUnit,
                        ),
                    ),
            ),
        )

        scheduleSyncPending()
        return dao.getWithRelationsById(entryId)?.toModel()
    }

    private fun scheduleSyncPending() {
        if (session.isSignedIn) scope.launch { syncPending() }
    }

    suspend fun syncPending(): List<Int> {
        if (session.client == null) return emptyList()
        if (syncPendingMutex.isLocked) return emptyList()

        return syncPendingMutex.withLock {
            val transactions = dao.getPendingTransactions()
            if (transactions.isEmpty()) return@withLock emptyList()

            val processedIds = mutableListOf<Int>()
            transactions.groupBy { it.entryId }.forEach { (entryId, actions) ->
                try {
                    val resolvedId = processEntryActions(entryId, actions)
                    if (resolvedId != null) processedIds.add(resolvedId)
                } catch (e: Exception) {
                    Logger.e(e, tag = "ShoppingRepository") {
                        "Failed to sync pending actions for entry $entryId"
                    }
                }
            }
            processedIds
        }
    }

    // process all actions done to an entry whilst offline
    // returns the (potentially new) ID of the entry
    private suspend fun processEntryActions(
        entryId: Int,
        actions: List<ShoppingTransactionEntity>,
    ): Int? {
        val client = session.client ?: return null

        val createAction = actions.firstOrNull { it.action == ShoppingListEntryOfflineActions.CREATE }
        val hasDelete = actions.any { it.action == ShoppingListEntryOfflineActions.DELETE }

        if (createAction != null) {
            if (hasDelete) {
                dao.delete(entryId)
                actions.forEach { dao.deleteTransaction(it.id) }
                return null
            }

            val createPayload =
                json.decodeFromString<ShoppingCreatePayload>(
                    createAction.payload ?: return null,
                )
            var finalAmount = createPayload.amount
            var finalChecked = createPayload.checked
            var finalUnitName = createPayload.unitName
            var finalClearUnit = false

            actions
                .asSequence()
                .dropWhile { it.id != createAction.id }
                .drop(1)
                .forEach { action ->
                    when (action.action) {
                        ShoppingListEntryOfflineActions.CHECK -> {
                            finalChecked = true
                        }

                        ShoppingListEntryOfflineActions.UNCHECK -> {
                            finalChecked = false
                        }

                        ShoppingListEntryOfflineActions.UPDATE -> {
                            val p =
                                action.payload?.let { payloadJson ->
                                    json.decodeFromString<ShoppingUpdatePayload>(payloadJson)
                                } ?: return@forEach
                            p.amount?.let { v -> finalAmount = v }
                            if (p.clearUnit) {
                                finalUnitName = null
                                finalClearUnit = true
                            } else if (p.unitName != null) {
                                finalUnitName = p.unitName
                                finalClearUnit = false
                            }
                        }

                        else -> {}
                    }
                }

            val serverEntry =
                client.shopping.add(
                    amount = finalAmount,
                    foodName = createPayload.foodName,
                    unitName = if (finalClearUnit) null else finalUnitName,
                    shoppingLists = createPayload.shoppingLists,
                    mealPlanId = createPayload.mealPlanId,
                    listRecipeId = createPayload.listRecipeId,
                    order = createPayload.order,
                    checked = finalChecked,
                )

            dao.delete(entryId)
            dao.upsertAll(listOf(serverEntry.toEntity()))
            actions.forEach { dao.deleteTransaction(it.id) }
            return serverEntry.id
        }

        val updates = actions.filter { it.action == ShoppingListEntryOfflineActions.UPDATE }
        if (updates.isNotEmpty()) {
            var finalAmount: Double? = null

            updates.forEach { tx ->
                val p =
                    tx.payload?.let { payloadJson ->
                        json.decodeFromString<ShoppingUpdatePayload>(payloadJson)
                    } ?: return@forEach
                p.amount?.let { finalAmount = it }
            }

            client.shopping.partialUpdate(
                entryId = entryId,
                amount = finalAmount,
            )
        }

        val lastStatusAction =
            actions.lastOrNull {
                it.action in
                    setOf(
                        ShoppingListEntryOfflineActions.CHECK,
                        ShoppingListEntryOfflineActions.UNCHECK,
                        ShoppingListEntryOfflineActions.DELETE,
                    )
            }
        when (lastStatusAction?.action) {
            ShoppingListEntryOfflineActions.CHECK -> {
                client.shopping.check(setOf(entryId))
            }

            ShoppingListEntryOfflineActions.UNCHECK -> {
                client.shopping.uncheck(setOf(entryId))
            }

            ShoppingListEntryOfflineActions.DELETE -> {
                client.shopping.delete(entryId)
            }

            else -> {}
        }

        actions.forEach { dao.deleteTransaction(it.id) }
        return entryId
    }

    companion object {
        private val OFFLINE_CREATED_BY =
            TandoorShoppingListEntryCreatedBy(
                id = 0,
                username = "",
                display_name = "",
            )
    }
}
