package de.kitshn.android.api.tandoor.route

import android.net.Uri
import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.TandoorRequestsError
import de.kitshn.android.api.tandoor.getArray
import de.kitshn.android.api.tandoor.getObject
import de.kitshn.android.api.tandoor.model.TandoorMealPlan
import de.kitshn.android.api.tandoor.model.TandoorMealType
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.android.api.tandoor.postObject
import de.kitshn.android.json
import de.kitshn.android.toTandoorDate
import kotlinx.serialization.encodeToString
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TandoorMealPlanRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    @Throws(TandoorRequestsError::class)
    suspend fun create(
        title: String = "",
        recipe: TandoorRecipeOverview? = null,
        servings: Int,
        note: String = "",
        from_date: LocalDate,
        to_date: LocalDate? = null,
        meal_type: TandoorMealType,
        addshopping: Boolean = false
    ): TandoorMealPlan {
        val data = JSONObject().apply {
            put("title", title)
            if(recipe != null) put(
                "recipe", JSONObject(
                    json.encodeToString(recipe)
                )
            )
            put("servings", servings)
            put("note", note)
            put("from_date", from_date.toTandoorDate())
            if(to_date != null) put("to_date", to_date.toTandoorDate())
            put(
                "meal_type", JSONObject(
                    json.encodeToString(meal_type)
                )
            )
            put("addshopping", addshopping)
        }

        val mealPlan = TandoorMealPlan.parse(
            this.client,
            client.postObject("/meal-plan/", data).toString()
        )

        mealPlan.client = client
        client.container.mealPlan[mealPlan.id] = mealPlan

        return mealPlan
    }

    @Throws(TandoorRequestsError::class)
    suspend fun fetch(
        from: LocalDate? = null,
        to: LocalDate? = null,
        meal_type: Int? = null
    ): List<TandoorMealPlan> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val builder = Uri.Builder().appendPath("meal-plan")
        if(from != null) builder.appendQueryParameter("from_date", formatter.format(from))
        if(to != null) builder.appendQueryParameter("to_date", formatter.format(to))
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

    @Throws(TandoorRequestsError::class)
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