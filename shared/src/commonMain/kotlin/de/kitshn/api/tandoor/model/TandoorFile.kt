package de.kitshn.api.tandoor.model

import de.kitshn.api.tandoor.route.TandoorUser
import kotlinx.serialization.Serializable

/**
 * A file uploaded to Tandoor (recipe attachments, space images, nav logos, …).
 * Every endpoint that exposes a user file uses this same shape, including a
 * server-generated [preview] URL.
 */
@Serializable
data class TandoorFile(
    val id: Int,
    val name: String? = null,
    val file_download: String,
    val preview: String? = null,
    val file_size_kb: Int? = null,
    val created_by: TandoorUser? = null,
    val created_at: String? = null,
)

typealias TandoorImage = TandoorFile
