package de.kitshn.android.api.tandoor

import android.content.Context
import de.kitshn.android.api.tandoor.route.TandoorCookLogRoute
import de.kitshn.android.api.tandoor.route.TandoorFoodRoute
import de.kitshn.android.api.tandoor.route.TandoorKeywordRoute
import de.kitshn.android.api.tandoor.route.TandoorMealPlanRoute
import de.kitshn.android.api.tandoor.route.TandoorMealTypeRoute
import de.kitshn.android.api.tandoor.route.TandoorRecipeBookRoute
import de.kitshn.android.api.tandoor.route.TandoorRecipeFromSourceRoute
import de.kitshn.android.api.tandoor.route.TandoorRecipeRoute
import de.kitshn.android.api.tandoor.route.TandoorShoppingRoute
import de.kitshn.android.api.tandoor.route.TandoorSystemRoute
import de.kitshn.android.json
import kotlinx.serialization.Serializable
import org.json.JSONObject

@Serializable
data class TandoorCredentialsToken(
    val token: String,
    val scope: String,
    val expires: String
)

@Serializable
data class TandoorCredentials(
    val instanceUrl: String,
    val username: String = "",
    val password: String = "",
    var token: TandoorCredentialsToken? = null
)

class TandoorClient(
    val context: Context,
    val credentials: TandoorCredentials
) {

    val container = TandoorContainer(this)
    val media = TandoorMedia(this)

    val cookLog = TandoorCookLogRoute(this)
    val keyword = TandoorKeywordRoute(this)
    val food = TandoorFoodRoute(this)
    val mealPlan = TandoorMealPlanRoute(this)
    val mealType = TandoorMealTypeRoute(this)
    val recipe = TandoorRecipeRoute(this)
    val recipeBook = TandoorRecipeBookRoute(this)
    val recipeFromSource = TandoorRecipeFromSourceRoute(this)
    val shopping = TandoorShoppingRoute(this)
    val system = TandoorSystemRoute(this)

    suspend fun login(): TandoorCredentialsToken? {
        val obj = JSONObject()
        obj.put("username", credentials.username)
        obj.put("password", credentials.password)

        try {
            val data = postObject("-token-auth/", obj)
            return json.decodeFromString<TandoorCredentialsToken>(data.toString())
        } catch(_: TandoorRequestsError) {
        }

        return null
    }

    suspend fun testConnection(ignoreAuth: Boolean): Boolean {
        try {
            getObject("")
            return true
        } catch(e: TandoorRequestsError) {
            if(ignoreAuth) return e.volleyError?.networkResponse?.statusCode == 403
            return false
        }
    }

}