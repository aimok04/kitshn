package de.kitshn.actions.handlers

import android.content.Intent
import de.kitshn.KitshnViewModel

fun KitshnViewModel.handleShortcut(
    intent: Intent
): Boolean {
    if(intent.action == Intent.ACTION_VIEW && (intent.dataString ?: "").startsWith("shortcut/")) {
        val routeId = intent.dataString!!.replaceFirst("shortcut/", "")

        if(listOf("mealplan", "shopping", "books").contains(routeId)) {
            mainSubNavHostController?.navigate(routeId)
            return true
        } else if(routeId == "shoppingMode") {
            navHostController?.navigate("shopping/shoppingMode")
            return true
        }
    }

    return false
}