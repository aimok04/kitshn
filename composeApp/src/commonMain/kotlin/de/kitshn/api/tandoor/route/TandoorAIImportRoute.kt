package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeImportResponse
import de.kitshn.api.tandoor.postMultipart
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
    ): TandoorRecipeImportResponse {
        val response = client.postMultipart(
            "/ai-import/"
        ) {
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

        return TandoorRecipeImportResponse.parse(
            client,
            response.bodyAsText()
        )
    }

}