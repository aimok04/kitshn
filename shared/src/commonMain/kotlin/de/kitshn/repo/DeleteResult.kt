package de.kitshn.repo

sealed interface DeleteResult {
    data object Deleted : DeleteResult
    data object InUse : DeleteResult
    data object NotFound : DeleteResult
}
