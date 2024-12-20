package de.kitshn

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorCredentials
import de.kitshn.ui.route.navigation.PrimaryNavigation
import de.kitshn.ui.theme.KitshnTheme
import de.kitshn.ui.theme.isDynamicColorSupported

private val SavedTandoorClient = mutableStateOf<TandoorClient?>(null)

@Composable
internal fun App(
    /**
     * calls when KitshnViewModel was created
     */
    onVmCreated: (vm: KitshnViewModel) -> Unit = { },

    /**
     * calls before potential onboarding. Aborts onboarding if true is returned
     */
    onBeforeCredentialsCheck: (credentials: TandoorCredentials?) -> Boolean = { false },

    /**
     * after onboarding checks and completed onboarding
     */
    onLaunched: () -> Unit = { }
) {
    val vm = remember { KitshnViewModel(
        defaultTandoorClient = SavedTandoorClient.value,
        onBeforeCredentialsCheck = onBeforeCredentialsCheck,
        onLaunched = onLaunched
    ).also { onVmCreated(it) } }

    DisposableEffect(key1 = Unit) {
        onDispose {
            SavedTandoorClient.value = vm.tandoorClient
        }
    }

    val dynamicColor = vm.settings.getEnableDynamicColors.collectAsState(initial = true)

    val systemTheme = vm.settings.getEnableSystemTheme.collectAsState(initial = true)
    val darkMode = vm.settings.getEnableDarkTheme.collectAsState(initial = true)

    KitshnTheme(
        darkTheme = if(systemTheme.value) isSystemInDarkTheme() else darkMode.value,
        dynamicColor = isDynamicColorSupported() && dynamicColor.value
    ) {
        Surface(
            Modifier.fillMaxSize()
        ) {
            PrimaryNavigation(vm = vm)
        }
    }

    LaunchedEffect(Unit) {
        vm.init()
    }
}
