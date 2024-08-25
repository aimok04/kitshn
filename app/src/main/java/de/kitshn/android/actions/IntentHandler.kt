package de.kitshn.android.actions

import android.content.Intent
import de.kitshn.android.KitshnViewModel
import de.kitshn.android.actions.handlers.handleAppLink
import de.kitshn.android.api.tandoor.TandoorCredentials

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

    handleAppLink(intent)

    // handle recipe url sharing
    if(intent.action == Intent.ACTION_SEND) {
        navigateTo("main", "home")
        uiState.importRecipeUrl.set(intent.getStringExtra(Intent.EXTRA_TEXT) ?: "")
    }
}