package de.kitshn.api.tandoor.model

import androidx.compose.ui.graphics.Color
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.api.tandoor.delete
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.api.tandoor.patchObject
import de.kitshn.json
import de.kitshn.parseTandoorDate
import de.kitshn.toTandoorDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import org.json.JSONObject
import java.time.LocalDate

@Serializable
data class TandoorMealType(
    val id: Int,
    val name: String,
    val order: Int,
    @SerialName("color")
    val colorStr: String? = null,
    val default: Boolean,
    val created_by: Int
) {
    @Transient
    val color = if((colorStr ?: "").isBlank()) {
        Color.Gray
    } else {
        Color(android.graphics.Color.parseColor(colorStr))
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

    @Throws(TandoorRequestsError::class)
    suspend fun delete(): String {
        return client?.delete("/meal-plan/${id}/") ?: "unknown"
    }

    suspend fun partialUpdate(
        title: String? = null,
        recipe: TandoorRecipeOverview? = null,
        servings: Int? = null,
        note: String? = null,
        from_date: LocalDate? = null,
        to_date: LocalDate? = null,
        meal_type: TandoorMealType? = null,
        addshopping: Boolean? = null
    ) {
        if(this.client == null) return

        val data = JSONObject().apply {
            if(title != null) put("title", title)
            if(recipe != null) put(
                "recipe", JSONObject(
                    json.encodeToString(recipe)
                )
            )
            if(servings != null) put("servings", servings)
            if(note != null) put("note", note)
            if(meal_type != null) put(
                "meal_type", JSONObject(
                    json.encodeToString(meal_type)
                )
            )
            if(addshopping != null) put("addshopping", addshopping)

            if(to_date != null) put("to_date", to_date.toTandoorDate())
            if(from_date != null) {
                put("from_date", from_date.toTandoorDate())

                if(to_date == null) {
                    val originalToDate = this@TandoorMealPlan.to_date.parseTandoorDate()
                    if(originalToDate.isBefore(from_date)) put("to_date", from_date.toTandoorDate())
                }
            }
        }

        client!!.patchObject("/meal-plan/${id}/", data)
    }
}