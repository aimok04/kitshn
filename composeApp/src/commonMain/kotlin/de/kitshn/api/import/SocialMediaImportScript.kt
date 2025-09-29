package de.kitshn.api.import

import co.touchlab.kermit.Logger
import com.multiplatform.webview.web.WebViewNavigator
import de.kitshn.json
import kitshn.composeapp.generated.resources.Res
import kotlinx.serialization.Serializable
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Serializable
data class SocialMediaImportScriptResponse(
    val description: String? = null,
    val imageURL: String? = null
)

suspend fun WebViewNavigator.runSocialMediaImportScript(): SocialMediaImportScriptResponse? {
    val js = Res.readBytes("files/social_media_import_script.js").decodeToString()

    val responseString = suspendCoroutine { cont ->
        evaluateJavaScript(
            script = js,
            callback = { response ->
                cont.resume(response)
            }
        )
    }

    Logger.d("SocialMediaImportScript.kt") { responseString }
    if(!responseString.startsWith("{")) return null

    return json.decodeFromString<SocialMediaImportScriptResponse>(responseString)
}