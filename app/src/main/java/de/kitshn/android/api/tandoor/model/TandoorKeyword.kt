package de.kitshn.android.api.tandoor.model

import kotlinx.serialization.Serializable

@Serializable
data class TandoorKeyword(
    val id: Int,
    val name: String,
    val label: String = "",
    val description: String? = "",
    val numchild: Int = 0,
    val created_at: String,
    val updated_at: String,
    val full_name: String = ""
)

@Serializable
data class TandoorKeywordOverview(
    val id: Int,
    val label: String
)