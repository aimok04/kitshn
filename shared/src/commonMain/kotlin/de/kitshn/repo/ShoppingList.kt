package de.kitshn.repo

import co.touchlab.kermit.Logger
import de.kitshn.AppDatabase
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingList
import de.kitshn.db.entity.toEntity
import de.kitshn.db.entity.toModel
import de.kitshn.session.TandoorSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class ShoppingListRepo(
    db: AppDatabase,
    private val session: TandoorSession,
    private val scope: CoroutineScope,
    periodicInterval: Duration? = null,
) : SyncableRepo(
    repoMetaDao = db.repoMetaDao(),
    periodicInterval = periodicInterval,
    reconcileInterval = 30.minutes,
) {
    override val repoMetaName: String = "shoppingList"
    override val reconcileCapabilities = emptySet<ReconcileCapability>()

    private val _shoppingLists = MutableStateFlow<List<TandoorShoppingList>>(emptyList())
    val shoppingLists = _shoppingLists.asStateFlow()

    private val dao = db.shoppingListDao()

    init {
        datasetSize = DatasetSize.FEW

        scope.launch {
            dao.getAllAsFlow().collect { entities ->
                _shoppingLists.value = entities.map { it.toModel() }
            }
        }
    }

    override suspend fun performFullReconcile(): ReconcileResult? {
        val client = session.client ?: return null
        return try {
            val remote = client.shopping.listAllLists().results
            dao.upsertAll(remote.map { it.toEntity() })
            dao.deleteSyncedNotIn(remote.map { it.id })
            ReconcileResult(nextPage = 1)
        } catch (e: TandoorRequestsError) {
            logSyncFailure(e, "reconcile")
            null
        }
    }
}
