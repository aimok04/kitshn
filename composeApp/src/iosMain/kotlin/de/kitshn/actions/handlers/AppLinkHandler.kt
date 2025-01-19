package de.kitshn.actions.handlers

import androidx.lifecycle.viewModelScope
import com.eygraber.uri.Uri
import de.kitshn.KitshnViewModel
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorCredentials
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
    val shareWrappingUrl = BuildConfig.SHARE_WRAPPER_URL

    val linkUrl = when {
        data.startsWith("kitshn://") -> data.substring(9)
        data.startsWith(shareWrappingUrl) -> data.substring(shareWrappingUrl.length)
        else -> null
    } ?: return false

    val linkUri = Uri.parse(linkUrl.let {
        if (it.startsWith("http://") || it.startsWith("https://")) {
            it
        } else {
            "https://$it"
        }
    })

    val linkArgs = linkUri.path?.split("/")?.toMutableList() ?: mutableListOf()
    if (linkArgs.firstOrNull()?.isBlank() == true) linkArgs.removeFirstOrNull()
    if (linkArgs.lastOrNull()?.isBlank() == true) linkArgs.removeLastOrNull()

    val instanceUri = credentials?.instanceUrl?.let { Uri.parse(it) }
    val matchingHosts = instanceUri?.host == linkUri.host

    // handle public accessible routes
    if (!matchingHosts && linkArgs.size == 4 && linkArgs[0] == "view" && linkArgs[1] == "recipe") {
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

    if (credentials == null || beforeCheck) return false

    // show error message if hosts don't match
    if (!matchingHosts) {
        navHostController?.navigate("alert/inaccessibleInstance")
        return true
    }

    if ((linkArgs.size == 3 || linkArgs.size == 4) && linkArgs[0] == "view" && linkArgs[1] == "recipe") {
        val recipeId = linkArgs[2]
        navHostController?.navigate("recipe/${recipeId}/view")
        return true
    }

    return false
}