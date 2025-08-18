package de.kitshn.actions.handlers

import androidx.lifecycle.viewModelScope
import com.eygraber.uri.Uri
import de.kitshn.KitshnViewModel
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorCredentials
import de.kitshn.extractUrl
import kitshn.composeApp.BuildConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * handle app links before credentials and onboarding checks
 */
fun KitshnViewModel.handleAppLink(
    credentials: TandoorCredentials?,
    data: String
): Boolean {
    return handleAppLinkImpl(
        credentials = credentials,
        beforeCheck = true,
        data = data
    )
}

/**
 * handle app links after credentials and onboarding checks
 */
fun KitshnViewModel.handleAppLink(
    data: String
): Boolean {
    return handleAppLinkImpl(
        credentials = tandoorClient?.credentials,
        beforeCheck = false,
        data = data
    )
}

private fun KitshnViewModel.handleAppLinkImpl(
    credentials: TandoorCredentials?,
    beforeCheck: Boolean,
    data: String
): Boolean {
    // import deep link (mainly for iOS)
    if (data.startsWith("kitshn://import/")) {
        if (beforeCheck) return false

        val text = data.removePrefix("kitshn://import/")
        val url = (text.extractUrl() ?: text.extractUrl("\n"))
            ?: return false

        navigateTo("main", "home")
        uiState.importRecipeUrl.set(url)
        return true
    }

    val shareWrappingUrl = BuildConfig.SHARE_WRAPPER_URL

    val linkUrl = when {
        data.startsWith("kitshn://") -> data.substring(9)
        data.startsWith(shareWrappingUrl) -> data.substring(shareWrappingUrl.length)
        else -> null
    } ?: return false

    val linkUri = Uri.parse(linkUrl.let {
        if(it.startsWith("https//")) {
            it.replaceFirst("https//", "https://")
        } else if(it.startsWith("http//")) {
            it.replaceFirst("http//", "http://")
        } else if(it.startsWith("http://") || it.startsWith("https://")) {
            it
        } else {
            "https://$it"
        }
    }.toString())

    val linkArgs = linkUri.path?.split("/")?.toMutableList() ?: mutableListOf()
    if (linkArgs.firstOrNull()?.isBlank() == true) linkArgs.removeFirstOrNull()
    if (linkArgs.lastOrNull()?.isBlank() == true) linkArgs.removeLastOrNull()

    val instanceUri = credentials?.instanceUrl?.let { Uri.parse(it) }
    val matchingHosts = instanceUri?.host == linkUri.host

    // handle public accessible routes (legacy)
    if(!matchingHosts && linkArgs.size == 4 && linkArgs[0] == "view" && linkArgs[1] == "recipe") {
        viewModelScope.launch {
            val origin = linkUri.scheme + "://" + linkUri.host
            val recipeId = linkArgs[2]
            val shareToken = linkArgs[3]

            uiState.shareClient =
                TandoorClient(TandoorCredentials(instanceUrl = origin))

            delay(100)
            navHostController?.navigate("recipe/${recipeId}/public/${shareToken}") {
                popUpTo("main") {
                    inclusive = true
                }
            }
        }
        return true
    }

    // handle public accessible routes (v2)
    if(!matchingHosts && linkArgs.size == 2 && linkArgs[0] == "recipe") {
        viewModelScope.launch {
            val origin = linkUri.scheme + "://" + linkUri.host
            val recipeId = linkArgs[1]
            val shareToken = linkUri.getQueryParameter("share")

            uiState.shareClient =
                TandoorClient(TandoorCredentials(instanceUrl = origin))

            delay(100)
            navHostController?.navigate("recipe/${recipeId}/public/${shareToken}") {
                popUpTo("main") {
                    inclusive = true
                }
            }
        }
        return true
    }

    if (credentials == null || beforeCheck) return false

    // show error message if hosts don't match
    if (!matchingHosts) {
        navHostController?.navigate("alert/inaccessibleInstance")
        return true
    }

    // legacy
    if((linkArgs.size == 3 || linkArgs.size == 4) && linkArgs[0] == "view" && linkArgs[1] == "recipe") {
        val recipeId = linkArgs[2]
        navHostController?.navigate("recipe/${recipeId}/view")
        return true
    }

    // v2
    if((linkArgs.size == 2 || linkArgs.size == 3) && linkArgs[0] == "recipe") {
        val recipeId = linkArgs[1]
        navHostController?.navigate("recipe/${recipeId}/view")
        return true
    }

    return false
}