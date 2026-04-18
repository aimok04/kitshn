package de.kitshn.api.tandoor.route

import androidx.compose.ui.graphics.Color
import com.materialkolor.ktx.toHex
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.TandoorMealType
import de.kitshn.api.tandoor.model.TandoorPagedResponse
import de.kitshn.api.tandoor.patchObject
import de.kitshn.api.tandoor.postObject
import de.kitshn.json
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement

class TandoorMealTypeRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun fetch(): List<TandoorMealType> {
        val response = json.decodeFromString<TandoorPagedResponse<TandoorMealType>>(
            client.getObject("/meal-type/?page_size=100").toString()
        )

        response.results.forEach {
            client.container.mealType[it.id] = it
        }

        return response.results
    }

    suspend fun create(
        name: String,
        order: Int?,
        time: LocalTime? = null,
        color: Color? = null,
    ): TandoorMealType {
        val data = buildJsonObject {
            put("name", JsonPrimitive(name))
            if(order != null) put("order", json.encodeToJsonElement(order))
            if(time != null) put("time", JsonPrimitive(time.toString()))
            if(color != null) put("color", JsonPrimitive(color.toHex()))
        }

        val mealType = TandoorMealType.parse(
            client.postObject("/meal-type/", data).toString()
        )

        client.container.mealType[mealType.id] = mealType

        return mealType
    }
}