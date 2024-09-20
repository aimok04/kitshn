package de.kitshn.android.actions.handlers

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewModelScope
import de.kitshn.android.KitshnViewModel
import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.TandoorCredentials
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * handle app links before credentials and onboarding checks
 */
fun KitshnViewModel.handleAppLink(
    credentials: TandoorCredentials?,
    intent: Intent
): Boolean {
    return handleAppLinkImpl(
        credentials = credentials,
        beforeCheck = true,
        intent = intent
    )
}

/**
 * handle app links after credentials and onboarding checks
 */
fun KitshnViewModel.handleAppLink(
    intent: Intent
): Boolean {
    return handleAppLinkImpl(
        credentials = tandoorClient?.credentials,
        beforeCheck = false,
        intent = intent
    )
}

private fun KitshnViewModel.handleAppLinkImpl(
    credentials: TandoorCredentials?,
    beforeCheck: Boolean,
    intent: Intent
): Boolean {
    // handle kshli.github.io and kitshn:// links
    if(intent.action == Intent.ACTION_VIEW && intent.dataString != null) {
        val data = intent.dataString!!

        val linkUrl = when {
            data.startsWith("kitshn://") -> data.substring(9)
            data.startsWith("https://kshli.github.io/#") -> data.substring(25)
            else -> null
        } ?: return false

        val linkUri = Uri.parse(linkUrl.let {
            if(it.startsWith("http://") || it.startsWith("https://")) {
                it
            } else {
                "https://$it"
            }
        })

        val linkArgs = linkUri.path?.split("/")?.toMutableList() ?: mutableListOf()
        if(linkArgs.firstOrNull()?.isBlank() == true) linkArgs.removeFirstOrNull()
        if(linkArgs.lastOrNull()?.isBlank() == true) linkArgs.removeLastOrNull()

        val instanceUri = credentials?.instanceUrl?.let { Uri.parse(it) }
        val matchingHosts = instanceUri?.host == linkUri.host

        // handle public accessible routes
        if(!matchingHosts && linkArgs.size == 4 && linkArgs[0] == "view" && linkArgs[1] == "recipe") {
            viewModelScope.launch {
                val origin = linkUri.scheme + "://" + linkUri.host
                val recipeId = linkArgs[2]
                val shareToken = linkArgs[3]

                uiState.shareClient =
                    TandoorClient(context, TandoorCredentials(instanceUrl = origin))

                delay(100)
                navHostController?.navigate("recipe/${recipeId}/public/${shareToken}") {
                    popUpTo("main") {
                        inclusive = true
                    }
                }
            }
            return true
        }

        // handle auth required routes
        if(credentials == null || beforeCheck) return false

        // show error message if hosts don't match
        if(!matchingHosts) {
            navHostController?.navigate("alert/inaccessibleInstance")
            return true
        }

        if((linkArgs.size == 3 || linkArgs.size == 4) && linkArgs[0] == "view" && linkArgs[1] == "recipe") {
            val recipeId = linkArgs[2]
            navHostController?.navigate("recipe/${recipeId}/view")
            return true
        }
    }

    return false
}