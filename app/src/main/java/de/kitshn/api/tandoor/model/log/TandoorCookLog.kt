package de.kitshn.api.tandoor.model.log

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class TandoorCookLogCreatedBy(
    val id: Int,
    val username: String,
    val first_name: String,
    val last_name: String,
    val display_name: String
)

@Serializable
class TandoorCookLog(
    val id: Int,
    val recipe: Int,
    val servings: Int? = null,
    val rating: Int? = null,
    val comment: String? = null,
    val created_by: TandoorCookLogCreatedBy,
    val created_at: String,
    val updated_at: String
) {

    @Transient
    var client: TandoorClient? = null

    companion object {
        fun parse(client: TandoorClient, data: String): TandoorCookLog {
            val obj = json.decodeFromString<TandoorCookLog>(data)
            obj.client = client
            return obj
        }
    }

}