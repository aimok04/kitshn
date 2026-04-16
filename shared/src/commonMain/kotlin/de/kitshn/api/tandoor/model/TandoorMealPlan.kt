package de.kitshn.api.tandoor.model

import androidx.compose.ui.graphics.Color
import com.materialkolor.ktx.toHex
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.delete
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.api.tandoor.patchObject
import de.kitshn.json
import de.kitshn.parseTandoorDate
import de.kitshn.toColorInt
import de.kitshn.toStartOfDayString
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
data class TandoorMealType(
    val id: Int,
    val name: String,
    val order: Int,
    val time: String? = null,
    @SerialName("color")
    val colorStr: String? = null,
    val created_by: Int
) {

    @Transient
    var client: TandoorClient? = null

    @Transient
    val color = if((colorStr ?: "").isBlank()) {
        Color.Gray
    } else {
        Color(colorStr?.toColorInt() ?: -1)
    }

    suspend fun delete(): String {
        return client?.delete("/meal-type/${id}/")?.status?.value?.toString() ?: "unknown"
    }

    suspend fun partialUpdate(
        name: String? = null,
        order: Int? = null,
        time: LocalTime? = null,
        color: Color? = null,
    ): TandoorMealType? {
        if(this.client == null) return null

        val data = buildJsonObject {
            if(name != null) put("name", JsonPrimitive(name))
            if(order != null) put("order", json.encodeToJsonElement(order))
            if(time != null) put("time", JsonPrimitive(time.toString()))
            if(color != null) put("colorStr", JsonPrimitive(color.toHex()))
        }

        return parse(client!!, client!!.patchObject("/meal-type/${id}/", data).toString())
    }

    companion object {
        fun parse(client: TandoorClient, data: String): TandoorMealType {
            val obj = json.decodeFromString<TandoorMealType>(data)
            obj.client = client
            return obj
        }
    }

}

@Serializable
class TandoorMealPlan(
    val id: Int,
    val title: String,
    val recipe: TandoorRecipeOverview? = null,
    val servings: Double,
    var note: String? = null,
    val note_markdown: String? = null,
    val from_date: String,
    val to_date: String,
    val meal_type: TandoorMealType,
    val created_by: Int,
    val recipe_name: String? = null,
    val meal_type_name: String,
    val shopping: Boolean
) {
    @Transient
    var client: TandoorClient? = null

    companion object {
        fun parse(client: TandoorClient, data: String): TandoorMealPlan {
            val obj = json.decodeFromString<TandoorMealPlan>(data)
            obj.client = client
            obj.recipe?.let { client.container.recipeOverview[it.id] = it }
            return obj
        }
    }

    suspend fun delete(): String {
        return client?.delete("/meal-plan/${id}/")?.status?.value?.toString() ?: "unknown"
    }

    suspend fun partialUpdate(
        title: String? = null,
        recipe: TandoorRecipeOverview? = null,
        servings: Double? = null,
        note: String? = null,
        from_date: LocalDate? = null,
        to_date: LocalDate? = null,
        meal_type: TandoorMealType? = null,
        addshopping: Boolean? = null
    ) {
        if(this.client == null) return

        val data = buildJsonObject {
            if(title != null) put("title", JsonPrimitive(title))
            if(recipe != null) put("recipe", json.encodeToJsonElement(recipe))
            if(servings != null) put("servings", JsonPrimitive(servings))
            if(note != null) put("note", JsonPrimitive(note))
            if(meal_type != null) put("meal_type", json.encodeToJsonElement(meal_type))
            if(addshopping != null) put("addshopping", JsonPrimitive(addshopping))

            if(to_date != null) put("to_date", JsonPrimitive(to_date.toStartOfDayString()))
            if(from_date != null) {
                put("from_date", JsonPrimitive(from_date.toStartOfDayString()))

                if(to_date == null) {
                    val originalToDate = this@TandoorMealPlan.to_date.parseTandoorDate()
                    if(originalToDate < from_date) put(
                        "to_date",
                        JsonPrimitive(from_date.toStartOfDayString())
                    )
                }
            }
        }

        client!!.patchObject("/meal-plan/${id}/", data)
    }
}