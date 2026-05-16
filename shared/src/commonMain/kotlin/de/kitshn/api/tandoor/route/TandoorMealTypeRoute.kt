package de.kitshn.api.tandoor.route

import androidx.compose.ui.graphics.Color
import com.materialkolor.ktx.toHex
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorMealType
import de.kitshn.api.tandoor.model.TandoorPagedResponse
import de.kitshn.api.tandoor.postObject
import de.kitshn.json
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement

private const val PATH = "meal-type/"
class TandoorMealTypeRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun list(
        page: Int = 1,
        pageSize: Int? = null,
    ): TandoorPagedResponse<TandoorMealType> {
        val response = listPage<TandoorMealType>(
            path = PATH,
            page = page,
            pageSize = pageSize,
        )

        response.results.forEach { cache(it) }

        return response
    }

    suspend fun listAll(
        onPageReceive: (suspend (List<TandoorMealType>) -> Boolean)? = null
    ): TandoorPagedResponse<TandoorMealType> = listAllPages(
        path = PATH,
        pageSize = 200,
    ) { page ->
        page.forEach { cache(it) }
        onPageReceive?.invoke(page) ?: false
    }

    suspend fun create(
        name: String,
        order: Int?,
        time: LocalTime? = null,
        color: Color? = null,
    ): TandoorMealType {
        val data = buildJsonObject {
            put("name", JsonPrimitive(name))
            if (order != null) put("order", json.encodeToJsonElement(order))
            if (time != null) put("time", JsonPrimitive(time.toString()))
            if (color != null) put("color", JsonPrimitive(color.toHex()))
        }

        val mealType = TandoorMealType.parse(
            client.postObject("/meal-type/", data).toString()
        )

        cache(mealType)

        return mealType
    }

    // TODO: remove once repo is in place
    private fun cache(mealType: TandoorMealType){
        client.container.mealType[mealType.id] = mealType
    }
}