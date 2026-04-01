package de.kitshn.actions

import android.content.Intent
import de.kitshn.KitshnViewModel
import de.kitshn.OAuthCallbackActivity
import de.kitshn.actions.handlers.handleAppLink
import de.kitshn.actions.handlers.handleShortcut
import de.kitshn.api.tandoor.TandoorCredentials
import de.kitshn.extractUrl

/**
 * handles intents before credentials and onboarding checks
 */
fun KitshnViewModel.preHandleIntent(
    credentials: TandoorCredentials?,
    intent: Intent
): Boolean {
    if(intent.action == Intent.ACTION_MAIN) return false
    // OAuth callbacks should not abort onboarding — just store the token
    if(handleOAuthCallbackIntent(intent)) return false
    return handleAppLink(credentials, intent)
}

/**
 * handles intents after credentials and onboarding checks
 */
fun KitshnViewModel.handleIntent(intent: Intent) {
    if(intent.action == Intent.ACTION_MAIN) return
    if(handleOAuthCallbackIntent(intent)) return

    val text = (intent.getStringExtra(Intent.EXTRA_TEXT) ?: "")

    if(handleShortcut(intent)) return
    if(handleAppLink(intent)) return

    // handle recipe url sharing
    if(intent.action == Intent.ACTION_SEND) {
        (text.extractUrl() ?: text.extractUrl("\n"))?.let { url ->
            navigateTo("main", "home")
            uiState.importRecipeUrl.set(url)
        }
    }
}

/**
 * Handles OAuth callback intents from OAuthCallbackActivity.
 * Callback URL format: kitshn://auth/callback?token={api_token}
 */
fun KitshnViewModel.handleOAuthCallbackIntent(intent: Intent): Boolean {
    if(intent.action != OAuthCallbackActivity.ACTION_OAUTH_CALLBACK) return false
    val uri = intent.data ?: return false

    val token = uri.getQueryParameter("token")
    if(token != null) {
        handleOAuthCallback(token)
        return true
    }

    return false
}