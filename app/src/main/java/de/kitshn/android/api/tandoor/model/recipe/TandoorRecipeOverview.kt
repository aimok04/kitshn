package de.kitshn.android.api.tandoor.model.recipe

import androidx.compose.runtime.Composable
import coil.request.ImageRequest
import de.kitshn.android.JsonAsStringSerializer
import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.model.TandoorKeywordOverview
import de.kitshn.android.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.json.JSONObject

@Serializable
class TandoorRecipeOverview(
    val id: Int,
    // lower than 128 characters
    val name: String,
    // lower than 512 characters
    val description: String? = null,
    // url to an image
    val image: String? = null,
    val keywords: List<TandoorKeywordOverview>,
    val working_time: Int,
    val waiting_time: Int,
    @Serializable(with = JsonAsStringSerializer::class) val created_by: String,
    val created_at: String,
    val updated_at: String,
    val internal: Boolean,
    val servings: Int,
    // lower or equal than 32 characters
    val servings_text: String,
    val rating: Double? = null,
    val last_cooked: String? = null,
    val new: Boolean? = false,
    val recent: String? = null
) {

    @Transient
    var client: TandoorClient? = null

    @Composable
    fun loadThumbnail(): ImageRequest? {
        return if(image == null || client == null) {
            null
        } else {
            return client!!.media.createImageBuilder(image)
                .crossfade(true)
                .build()
        }
    }

    companion object {
        fun parse(client: TandoorClient, data: JSONObject): TandoorRecipeOverview {
            val obj = json.decodeFromString<TandoorRecipeOverview>(data.toString())
            obj.client = client
            return obj
        }
    }

}