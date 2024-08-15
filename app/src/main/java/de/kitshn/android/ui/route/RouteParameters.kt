package de.kitshn.android.ui.route

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavBackStackEntry
import de.kitshn.android.KitshnViewModel

data class RouteParameters(
    val vm: KitshnViewModel,
    val bse: NavBackStackEntry,
    val onBack: (() -> Unit)? = null,
    val pv: PaddingValues = PaddingValues(),
    val goToMealPlanView: () -> Unit = {}
)