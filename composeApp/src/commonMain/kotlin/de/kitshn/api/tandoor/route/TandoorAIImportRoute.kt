package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeFromSource
import de.kitshn.api.tandoor.postMultipart
import de.kitshn.json
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

class TandoorAIImportRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    data class File(
        val name: String,
        val byteArray: ByteArray,
        val mimeType: String
    )

    suspend fun fetch(
        file: File?,
        text: String?
    ): TandoorRecipeFromSource {
        val response = client.postMultipart(
            "/ai-import/"
        ) {
            // needed for server v2.0.2 and up
            append("recipe_id", value = "")

            if(file != null) {
                append("file", value = file.byteArray, headers = Headers.build {
                    append(HttpHeaders.ContentType, file.mimeType)
                    append(HttpHeaders.ContentDisposition, "filename=${file.name}")
                })
            } else {
                append("file", value = "")
            }

            append("text", value = text ?: "")
        }

        val recipeFromSource = json.decodeFromString<TandoorRecipeFromSource>(response.bodyAsText())
        recipeFromSource.client = client
        return recipeFromSource
    }

}