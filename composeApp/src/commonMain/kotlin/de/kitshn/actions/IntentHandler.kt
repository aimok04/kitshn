package de.kitshn.actions

import android.content.Intent
import de.kitshn.KitshnViewModel
import de.kitshn.actions.handlers.handleAppLink
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
    return handleAppLink(credentials, intent)
}

/**
 * handles intents after credentials and onboarding checks
 */
fun KitshnViewModel.handleIntent(intent: Intent) {
    if(intent.action == Intent.ACTION_MAIN) return
    val text = (intent.getStringExtra(Intent.EXTRA_TEXT) ?: "")

    handleAppLink(intent)

    // handle recipe url sharing
    if(intent.action == Intent.ACTION_SEND) {
        (text.extractUrl() ?: text.extractUrl("\n"))?.let { url ->
            navigateTo("main", "home")
            uiState.importRecipeUrl.set(url)
        }
    }
}