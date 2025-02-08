package de.kitshn.ui.route.main

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
actual fun rememberAlternateNavController(): NavHostController {
    return rememberNavController()
}

actual fun clearRememberAlternateNavController() {}