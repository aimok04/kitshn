package de.kitshn.repo

import co.touchlab.kermit.Logger
import de.kitshn.AppDatabase
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingList
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntryCreatedBy
import de.kitshn.db.entity.ShoppingItemEntity
import de.kitshn.db.entity.ShoppingListEntryOfflineActions.CHECK
import de.kitshn.db.entity.ShoppingListEntryOfflineActions.DELETE
import de.kitshn.db.entity.ShoppingListEntryOfflineActions.UNCHECK
import de.kitshn.db.entity.ShoppingListEntryOfflineActions.UPDATE_AMOUNT
import de.kitshn.db.entity.ShoppingListEntryOfflineActions.UPDATE_UNIT
import de.kitshn.db.entity.ShoppingTransactionEntity
import de.kitshn.db.entity.toEntity
import de.kitshn.db.entity.toModel
import de.kitshn.session.TandoorSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val OFFLINE_CREATED_BY = TandoorShoppingListEntryCreatedBy(
    id = 0, username = "", display_name = "",
)

private val TERMINAL_ACTIONS = setOf(CHECK, UNCHECK, DELETE)
private val FIELD_ACTIONS = setOf(UPDATE_AMOUNT, UPDATE_UNIT)

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
    private val foodRepo: FoodRepo,
    private val supermarketCategoryRepo: SupermarketCategoryRepo,
    private val session: TandoorSession,
    private val scope: CoroutineScope,
    periodicInterval: Duration? = null,
) : SyncableRepo(
    repoMetaDao = db.repoMetaDao(),
    periodicInterval = periodicInterval,
    reconcileInterval = 10.seconds,
) {
    override val repoMetaName: String = "shopping"
    override val reconcileCapabilities = emptySet<ReconcileCapability>()

    init { datasetSize = DatasetSize.FEW }

    private val dao = db.shoppingDao()

    fun observe(): Flow<List<TandoorShoppingListEntry>> =
        dao.getAllAsFlow().map { entries -> entries.mapNotNull { it.toModel() } }

    override suspend fun syncPendingBefore() {
        syncDirty.value = false
        syncPending()
    }

    override suspend fun performInteractiveReconcile(): ReconcileResult? {
        syncPending()
        return performFullReconcile()
    }

    override suspend fun performFullReconcile(): ReconcileResult? {
        val client = session.client ?: return null
        val remoteItems = client.shopping.listAll().results

        supermarketCategoryRepo.upsertAll(remoteItems.mapNotNull { it.food.supermarket_category })
        foodRepo.upsertShoppingListEntryFoods(remoteItems.map { it.food })
        unitRepo.upsertAll(remoteItems.mapNotNull { it.unit })

        val protectedIds = dao.getPendingTransactions().map { it.entryId }.distinct().toSet()

        for (entry in remoteItems) {
            val existingLocalId = dao.localIdByRemoteId(entry.id)
            if (existingLocalId != null && existingLocalId in protectedIds) continue
            val foodLocalId = foodRepo.localIdByRemoteId(entry.food.id) ?: continue
            val unitLocalId = entry.unit?.id?.let { unitRepo.localIdByRemoteId(it) }
            dao.upsertByRemoteId(entry.toEntity(foodLocalId, unitLocalId))
        }

        return ReconcileResult(nextPage = 1)
    }

    suspend fun create(
        data: TandoorShoppingListEntryCreationRequest
    ): TandoorShoppingListEntry? {
        val foodId = foodRepo.findOrCreate(data.foodName).id
        val unitId = data.unitName?.takeIf { it.isNotBlank() }?.let { unitRepo.findOrCreate(it).id }
        val localId = dao.insertReturningId(
            ShoppingItemEntity(
                list_recipe = data.listRecipeId,
                shopping_lists = data.shoppingLists,
                food_id = foodId,
                unit_id = unitId,
                amount = data.amount,
                order = data.order ?: 0L,
                checked = data.checked,
                created_by = OFFLINE_CREATED_BY,
                meal_plan_id = data.mealPlanId,
            )
        ).toInt()
        scheduleSyncPending()
        return dao.getWithRelationsById(localId)?.toModel()
    }

    suspend fun createBulk(entries: List<TandoorShoppingListEntryCreationRequest>) {
        entries.forEach { create(it) }
    }

    /** Allows `null` to clear amount (sets it to 0) */
    suspend fun updateAmount(entryId: Int, amount: Double?): TandoorShoppingListEntry? {
        dao.updateAmount(entryId, amount ?: 0.0)
        dao.insertTransaction(ShoppingTransactionEntity(entryId = entryId, action = UPDATE_AMOUNT))
        scheduleSyncPending()
        return dao.getWithRelationsById(entryId)?.toModel()
    }

    /** Allows `null` to clear the unit */
    suspend fun updateUnit(entryId: Int, unitName: String?): TandoorShoppingListEntry? {
        val unitId = unitName?.takeIf { it.isNotBlank() }?.let { unitRepo.findOrCreate(it).id }
        dao.updateUnit(entryId, unitId)
        dao.insertTransaction(ShoppingTransactionEntity(entryId = entryId, action = UPDATE_UNIT))
        scheduleSyncPending()
        return dao.getWithRelationsById(entryId)?.toModel()
    }

    suspend fun toggleCheck(entryId: Int, checked: Boolean) =
        toggleCheckBulk(listOf(entryId), checked)

    suspend fun toggleCheckBulk(entryIds: Collection<Int>, checked: Boolean) {
        if (entryIds.isEmpty()) return
        val action = if (checked) CHECK else UNCHECK
        entryIds.forEach { id ->
            dao.updateChecked(id, checked)
            dao.insertTransaction(ShoppingTransactionEntity(entryId = id, action = action))
        }
        scheduleSyncPending()
    }

    suspend fun delete(entryId: Int) = deleteBulk(listOf(entryId))

    suspend fun deleteBulk(entryIds: List<Int>) {
        if (entryIds.isEmpty()) return
        entryIds.forEach { id ->
            dao.insertTransaction(ShoppingTransactionEntity(entryId = id, action = DELETE))
        }
        scheduleSyncPending()
    }

    suspend fun deleteAll(onlyChecked: Boolean = false) {
        val ids = observe().first()
            .filter { if (onlyChecked) it.checked else true }
            .map { it.id }
        deleteBulk(ids)
    }

    private suspend fun syncPending() {
        val client = session.client ?: return
        syncPendingCreates(client)
        syncPendingMutations(client)
    }

    private suspend fun syncPendingCreates(client: TandoorClient) {
        for (entry in dao.getPendingCreates()) {
            val item = entry.item
            val food = entry.food?.food ?: continue
            val unit = entry.unit

            val server = runPush(item.id) {
                client.shopping.add(
                    amount = item.amount,
                    foodName = food.name,
                    foodId = food.remoteId,
                    unitName = unit?.name,
                    unitId = unit?.remoteId,
                    shoppingLists = item.shopping_lists,
                    mealPlanId = item.meal_plan_id,
                    listRecipeId = item.list_recipe,
                    order = item.order,
                    checked = item.checked,
                )
            } ?: continue

            foodRepo.upsertShoppingListEntryFoods(listOf(server.food))
            server.unit?.let { unitRepo.upsertAll(listOf(it)) }
            val foodLocalId = foodRepo.localIdByRemoteId(server.food.id)
            if (foodLocalId == null) {
                Logger.w(tag = repoTag) { "foodLocalId missing after upsert — remoteId=${server.food.id}" }
                continue
            }
            val unitLocalId = server.unit?.id?.let { unitRepo.localIdByRemoteId(it) }
            val resolved = dao.upsertByRemoteId(
                server.toEntity(foodLocalId, unitLocalId, localId = item.id)
            )
            if (resolved != item.id) dao.delete(item.id)
            dao.deleteTransactionsForEntry(item.id)
        }
    }

    private suspend fun syncPendingMutations(client: TandoorClient) {
        val transactions = dao.getPendingTransactions()
        if (transactions.isEmpty()) return

        transactions.groupBy { it.entryId }.forEach { (entryId, actions) ->
            val remoteId = dao.remoteIdByLocalId(entryId)
            if (remoteId == null) {
                if (actions.any { it.action == DELETE }) dao.delete(entryId)
                return@forEach
            }

            if (actions.any { it.action in FIELD_ACTIONS }) {
                if (!pushFieldUpdates(client, entryId, remoteId, actions)) return@forEach
            }

            val terminal = actions.lastOrNull { it.action in TERMINAL_ACTIONS }
            if (terminal != null) {
                val pushed = runPush(entryId) {
                    when (terminal.action) {
                        CHECK -> client.shopping.check(setOf(remoteId))
                        UNCHECK -> client.shopping.uncheck(setOf(remoteId))
                        DELETE -> {
                            client.shopping.delete(remoteId)
                            dao.delete(entryId)
                        }
                        else -> Unit
                    }
                }
                if (pushed == null) return@forEach
            }

            actions.forEach { dao.deleteTransaction(it.id) }
        }
    }

    private suspend fun pushFieldUpdates(
        client: TandoorClient,
        entryId: Int,
        remoteId: Int,
        actions: List<ShoppingTransactionEntity>,
    ): Boolean {
        val dirtyAmount = actions.any { it.action == UPDATE_AMOUNT }
        val dirtyUnit = actions.any { it.action == UPDATE_UNIT }
        val row = dao.getWithRelationsById(entryId) ?: return true
        val item = row.item
        val unit = row.unit

        return runPush(entryId) {
            client.shopping.partialUpdate(
                entryId = remoteId,
                amount = if (dirtyAmount) item.amount else null,
                unitId = if (dirtyUnit) unit?.remoteId else null,
                unitName = if (dirtyUnit && unit != null && unit.remoteId == null) unit.name else null,
                clearUnit = dirtyUnit && unit == null,
            )
        } != null
    }

    private suspend fun <T> runPush(entryId: Int, block: suspend () -> T): T? = try {
        val result = block()
        dao.updateSyncError(entryId, null)
        result
    } catch (e: TandoorRequestsError) {
        if (e.isNetworkFailure) {
            Logger.w(e, tag = repoTag) { "Network error pushing entry $entryId; leaving queued" }
        } else {
            val msg = "Server refused push for entry $entryId: ${e.message}"
            Logger.e(e, tag = repoTag) { msg }
            dao.updateSyncError(entryId, msg)
        }
        null
    }

    private val syncDirty = MutableStateFlow(false)

    override fun shouldRunAgain(): Boolean = syncDirty.value

    private fun scheduleSyncPending() {
        if (!session.isSignedIn) return
        syncDirty.value = true
        scope.launch { sync() }
    }
}
