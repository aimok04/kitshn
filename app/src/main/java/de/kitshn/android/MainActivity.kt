package de.kitshn.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import de.kitshn.android.actions.handleIntent
import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.ui.route.navigation.PrimaryNavigation
import de.kitshn.android.ui.theme.KitshnTheme

private val SavedTandoorClient = mutableStateOf<TandoorClient?>(null)

class MainActivity : ComponentActivity() {

    var vm: KitshnViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        this.vm = KitshnViewModel(application, this, intent, SavedTandoorClient.value)

        setContent {
            val dynamicColor = vm!!.settings.getEnableDynamicColors.collectAsState(initial = true)

            val systemTheme = vm!!.settings.getEnableSystemTheme.collectAsState(initial = true)
            val darkMode = vm!!.settings.getEnableDarkTheme.collectAsState(initial = true)

            KitshnTheme(
                darkTheme = if(systemTheme.value) isSystemInDarkTheme() else darkMode.value,
                dynamicColor = dynamicColor.value
            ) {
                Surface(
                    Modifier.fillMaxSize()
                ) {
                    PrimaryNavigation(vm = vm!!)
                }
            }

            DisposableEffect(key1 = Unit) {
                onDispose {
                    SavedTandoorClient.value = vm!!.tandoorClient
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        vm?.handleIntent(intent)
        super.onNewIntent(intent)
    }

    override fun onPause() {
        vm?.uiState?.isInForeground = false
        super.onPause()
    }

    override fun onResume() {
        vm?.uiState?.isInForeground = true
        super.onResume()
    }
}