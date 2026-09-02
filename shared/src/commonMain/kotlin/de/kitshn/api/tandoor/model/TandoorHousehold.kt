package de.kitshn.api.tandoor.model

import de.kitshn.api.tandoor.route.TandoorAIProvider
import kotlinx.serialization.Serializable

@Serializable
data class TandoorHousehold(
    val id: Int,
    val name: String,
    val created_at: String,
    val updated_at: String,
)