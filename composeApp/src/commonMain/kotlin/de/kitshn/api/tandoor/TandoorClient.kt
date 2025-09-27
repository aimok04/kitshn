package de.kitshn.api.tandoor

import de.kitshn.api.tandoor.route.TandoorAIImportRoute
import de.kitshn.api.tandoor.route.TandoorAIProviderRoute
import de.kitshn.api.tandoor.route.TandoorCookLogRoute
import de.kitshn.api.tandoor.route.TandoorFoodRoute
import de.kitshn.api.tandoor.route.TandoorKeywordRoute
import de.kitshn.api.tandoor.route.TandoorMealPlanRoute
import de.kitshn.api.tandoor.route.TandoorMealTypeRoute
import de.kitshn.api.tandoor.route.TandoorRecipeBookRoute
import de.kitshn.api.tandoor.route.TandoorRecipeFromSourceRoute
import de.kitshn.api.tandoor.route.TandoorRecipeRoute
import de.kitshn.api.tandoor.route.TandoorServerSettingsRoute
import de.kitshn.api.tandoor.route.TandoorShoppingRoute
import de.kitshn.api.tandoor.route.TandoorSpaceRoute
import de.kitshn.api.tandoor.route.TandoorStepRoute
import de.kitshn.api.tandoor.route.TandoorSupermarketRoute
import de.kitshn.api.tandoor.route.TandoorUnitRoute
import de.kitshn.api.tandoor.route.TandoorUserPreferenceRoute
import de.kitshn.api.tandoor.route.TandoorUserRoute
import de.kitshn.json
import io.ktor.client.HttpClient
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put

@Serializable
data class TandoorCredentialsCustomHeader(
    var field: String,
    var value: String
)

@Serializable
data class TandoorCredentialsToken(
    val token: String,
    val scope: String,
    val expires: String
)

@Serializable
data class TandoorCredentials(
    val instanceUrl: String,
    var username: String = "",
    val password: String = "",
    var token: TandoorCredentialsToken? = null,
    val cookie: String? = null,
    val customHeaders: List<TandoorCredentialsCustomHeader> = listOf()
)

class TandoorClient(
    val credentials: TandoorCredentials
) {

    val httpClient = HttpClient {
        followRedirects = true
    }

    val container = TandoorContainer(this)
    val media = TandoorMedia(this)

    val aiImport = TandoorAIImportRoute(this)
    val aiProvider = TandoorAIProviderRoute(this)
    val cookLog = TandoorCookLogRoute(this)
    val keyword = TandoorKeywordRoute(this)
    val food = TandoorFoodRoute(this)
    val mealPlan = TandoorMealPlanRoute(this)
    val mealType = TandoorMealTypeRoute(this)
    val recipe = TandoorRecipeRoute(this)
    val recipeBook = TandoorRecipeBookRoute(this)
    val recipeFromSource = TandoorRecipeFromSourceRoute(this)
    val shopping = TandoorShoppingRoute(this)
    val space = TandoorSpaceRoute(this)
    val step = TandoorStepRoute(this)
    val supermarket = TandoorSupermarketRoute(this)
    val unit = TandoorUnitRoute(this)
    val user = TandoorUserRoute(this)
    val userPreference = TandoorUserPreferenceRoute(this)

    val serverSettings = TandoorServerSettingsRoute(this)

    suspend fun login(): TandoorCredentialsToken? {
        val obj = buildJsonObject {
            put("username", credentials.username)
            put("password", credentials.password)
        }

        try {
            return json.decodeFromJsonElement<TandoorCredentialsToken>(
                postObject(
                    "-token-auth/",
                    obj
                )
            )
        } catch(_: TandoorRequestsError) {
        }

        return null
    }

    suspend fun testConnection(ignoreAuth: Boolean): Boolean {
        try {
            getObject("/")
            return true
        } catch(e: TandoorRequestsError) {
            if(ignoreAuth) return e.response?.status == HttpStatusCode.Forbidden
            return false
        } catch(_: SerializationException) {
            return false
        }
    }

}