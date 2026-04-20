package de.kitshn.ui.route

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavBackStackEntry
import de.kitshn.KitshnViewModel

data class RouteParameters(
    val vm: KitshnViewModel,
    val bse: NavBackStackEntry,
    val onBack: (() -> Unit)? = null,
    val pv: PaddingValues = PaddingValues(),
    val goToMealPlanView: () -> Unit = {}
)