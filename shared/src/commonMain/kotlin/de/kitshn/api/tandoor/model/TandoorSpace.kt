package de.kitshn.api.tandoor.model

import de.kitshn.api.tandoor.route.TandoorAIProvider
import kotlinx.serialization.Serializable

@Serializable
data class TandoorSpace(
    val id: Int,
    val name: String,
    val created_at: String,
    val message: String,
    val ai_enabled: Boolean = false,
    val ai_default_provider: TandoorAIProvider? = null
)