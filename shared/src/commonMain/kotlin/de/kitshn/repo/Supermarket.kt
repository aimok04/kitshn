package de.kitshn.repo

import de.kitshn.AppDatabase
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarket
import de.kitshn.db.entity.SupermarketCategoryToSupermarketEntity
import de.kitshn.db.entity.toEntity
import de.kitshn.db.entity.toModel
import de.kitshn.session.TandoorSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class SupermarketRepo(
    db: AppDatabase,
    private val supermarketCategoryRepo: SupermarketCategoryRepo,
    private val session: TandoorSession,
    private val scope: CoroutineScope,
    periodicInterval: Duration? = null,
) : SyncableRepo(
    repoMetaDao = db.repoMetaDao(),
    periodicInterval = periodicInterval,
    reconcileInterval = 30.minutes,
) {
    override val repoMetaName: String = "supermarket"
    override val reconcileCapabilities = emptySet<ReconcileCapability>()

    private val _supermarkets = MutableStateFlow<List<TandoorSupermarket>>(emptyList())
    val supermarkets = _supermarkets.asStateFlow()

    private val dao = db.supermarketDao()

    init {
        datasetSize = DatasetSize.FEW

        scope.launch {
            dao.getAllWithCategoriesAsFlow().collect { rows ->
                _supermarkets.value = rows.map { it.toModel() }
            }
        }
    }

    override suspend fun performFullReconcile(): ReconcileResult? {
        val client = session.client ?: return null
        return try {
            val supermarkets = client.supermarket.listAll().results

            supermarketCategoryRepo.upsertAll(
                supermarkets.flatMap { it.category_to_supermarket }.map { it.category }
            )

            dao.deleteSupermarketsNotIn(supermarkets.map { it.id })

            for (supermarket in supermarkets) {
                val supermarketLocalId = dao.upsertByRemoteId(supermarket.toEntity())
                markItemSynced(supermarketLocalId)

                val joins = supermarket.category_to_supermarket.mapNotNull { join ->
                    val categoryRemoteId = join.category.id ?: return@mapNotNull null
                    val localCategoryId = supermarketCategoryRepo.localIdByRemoteId(categoryRemoteId)
                        ?: return@mapNotNull null
                    SupermarketCategoryToSupermarketEntity(
                        id = join.id,
                        supermarketLocalId = supermarketLocalId,
                        categoryLocalId = localCategoryId,
                        order = join.order,
                    )
                }
                dao.upsertJoins(joins)
                dao.deleteJoinsForSupermarketNotIn(supermarketLocalId, joins.map { it.id })
            }

            ReconcileResult(nextPage = 1)
        } catch (e: TandoorRequestsError) {
            logSyncFailure(e, "reconcile")
            null
        }
    }
}
