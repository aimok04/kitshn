package de.kitshn

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import coil3.compose.LocalPlatformContext
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorCredentials
import de.kitshn.cache.ShoppingListEntriesCache
import de.kitshn.ui.route.navigation.PrimaryNavigation
import de.kitshn.ui.theme.KitshnTheme
import de.kitshn.ui.theme.custom.AvailableColorSchemes
import kotlinx.coroutines.delay

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
    var deleteViewModel by remember { mutableStateOf(false) }
    LaunchedEffect(deleteViewModel) {
        if(!deleteViewModel) return@LaunchedEffect
        delay(1)
        deleteViewModel = false
    }

    if(deleteViewModel) return

    val vm = remember {
        KitshnViewModel(
            defaultTandoorClient = SavedTandoorClient.value,
            onBeforeCredentialsCheck = onBeforeCredentialsCheck,
            onLaunched = onLaunched
        ).also { onVmCreated(it) }
    }

    val density = LocalDensity.current

    DisposableEffect(key1 = Unit) {
        onDispose {
            SavedTandoorClient.value = vm.tandoorClient
        }
    }

    val colorSchemeName = vm.settings.getColorScheme.collectAsState(initial = null)
    var colorScheme by remember { mutableStateOf(AvailableColorSchemes.getDefault()) }
    LaunchedEffect(colorSchemeName.value) {
        if(colorSchemeName.value == null) return@LaunchedEffect
        AvailableColorSchemes.parse(colorSchemeName.value!!)?.let { colorScheme = it }
    }

    val customColorSchemeSeedInt =
        vm.settings.getCustomColorSchemeSeed.collectAsState(initial = null)
    var customColorSchemeSeed by remember { mutableStateOf(Color.Yellow) }
    LaunchedEffect(customColorSchemeSeedInt.value) {
        if(customColorSchemeSeedInt.value == null) return@LaunchedEffect
        customColorSchemeSeed = Color(customColorSchemeSeedInt.value!!)
    }

    val systemTheme = vm.settings.getEnableSystemTheme.collectAsState(initial = true)
    val darkMode = vm.settings.getEnableDarkTheme.collectAsState(initial = true)

    val alphaAnim = Animatable(1f)
    LaunchedEffect(vm.uiState.blockUI) {
        alphaAnim.animateTo(if(vm.uiState.blockUI) 0f else 1f, tween(200))
    }

    LaunchedEffect(vm.uiState.deleteViewModel) { deleteViewModel = vm.uiState.deleteViewModel }

    KitshnTheme(
        darkTheme = if(systemTheme.value) isSystemInDarkTheme() else darkMode.value,
        customColorSchemeSeed = customColorSchemeSeed,
        colorScheme = colorScheme
    ) {
        Surface(
            Modifier.fillMaxSize()
                .alpha(alphaAnim.value)
        ) {
            if(alphaAnim.value > 0f) PrimaryNavigation(vm = vm)

            Box {
                // display offline bar
                if(vm.uiState.offlineState.isOffline) Box(
                    modifier = Modifier
                        .height(with(density) {
                            WindowInsets.statusBars.getTop(density).toDp()
                        })
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer)
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        vm.init()

        while(true) {
            delay(8000)
            vm.connectivityCheck()
        }
    }

    // purge shopping list cache
    if(vm.tandoorClient == null) return

    val platformContext = LocalPlatformContext.current
    val shoppingListEntriesCache = remember {
        ShoppingListEntriesCache(platformContext, vm.tandoorClient!!)
    }

    LaunchedEffect(Unit) {
        shoppingListEntriesCache.purgeCache()
    }
}
