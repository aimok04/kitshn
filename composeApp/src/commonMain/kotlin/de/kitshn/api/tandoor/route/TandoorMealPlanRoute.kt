package de.kitshn.api.tandoor.route

import com.eygraber.uri.Uri
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getArray
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.TandoorMealPlan
import de.kitshn.api.tandoor.model.TandoorMealType
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.api.tandoor.postObject
import de.kitshn.json
import de.kitshn.toTandoorDate
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement

class TandoorMealPlanRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun create(
        title: String = "",
        recipe: TandoorRecipeOverview? = null,
        servings: Int,
        note: String = "",
        from_date: LocalDate,
        to_date: LocalDate? = null,
        meal_type: TandoorMealType,
        addshopping: Boolean = false,
        shared: List<TandoorUser>? = null
    ): TandoorMealPlan {
        val data = buildJsonObject {
            put("title", JsonPrimitive(title))
            if(recipe != null) put("recipe", json.encodeToJsonElement(recipe))
            put("servings", JsonPrimitive(servings))
            put("note", JsonPrimitive(note))
            put("from_date", JsonPrimitive(from_date.toTandoorDate()))
            if(to_date != null) put("to_date", JsonPrimitive(to_date.toTandoorDate()))
            put("meal_type", json.encodeToJsonElement(meal_type))
            put("addshopping", JsonPrimitive(addshopping))
            shared?.let {
                val sharedObj = json.encodeToJsonElement(shared)
                put("shared", sharedObj)
            }
        }

        val mealPlan = TandoorMealPlan.parse(
            this.client,
            client.postObject("/meal-plan/", data).toString()
        )

        mealPlan.client = client
        client.container.mealPlan[mealPlan.id] = mealPlan

        return mealPlan
    }

    @OptIn(FormatStringsInDatetimeFormats::class)
    suspend fun fetch(
        from: LocalDate? = null,
        to: LocalDate? = null,
        meal_type: Int? = null
    ): List<TandoorMealPlan> {
        val dateTimeFormat = LocalDate.Format {
            byUnicodePattern("yyyy-MM-dd")
        }

        val builder = Uri.Builder().appendEncodedPath("meal-plan/")
        if(from != null) builder.appendQueryParameter("from_date", dateTimeFormat.format(from))
        if(to != null) builder.appendQueryParameter("to_date", dateTimeFormat.format(to))
        if(meal_type != null) builder.appendQueryParameter("meal_type", meal_type.toString())

        val response = json.decodeFromString<List<TandoorMealPlan>>(
            client.getArray(builder.build().toString()).toString()
        )

        response.forEach {
            it.client = client
            it.recipe?.client = client

            client.container.mealType[it.meal_type.id] = it.meal_type
            client.container.mealPlan[it.id] = it
        }

        return response
    }

    suspend fun get(
        id: Int
    ): TandoorMealPlan {
        val mealPlan = TandoorMealPlan.parse(
            this.client,
            client.getObject("/meal-plan/${id}/").toString()
        )

        client.container.mealPlan[mealPlan.id] = mealPlan
        return mealPlan
    }

}