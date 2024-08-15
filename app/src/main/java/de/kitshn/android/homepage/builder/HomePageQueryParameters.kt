package de.kitshn.android.homepage.builder

import kotlinx.serialization.Serializable

@Serializable
data class HomePageQueryParameters(
    val query: String? = null,
    val new: Boolean? = null,
    val random: Boolean? = null,
    val keywords: List<String>? = null,
    val foods: List<String>? = null,
    val rating: Int? = null,
    val timescooked: Int? = null,
)
