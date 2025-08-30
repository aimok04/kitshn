package de.kitshn

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.kitshn.actions.handleIntent
import de.kitshn.actions.preHandleIntent

class AppActivity : ComponentActivity() {
    var vm: KitshnViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settings = SettingsViewModel()

        enableEdgeToEdge()
        setContent {
            App(
                onVmCreated = {
                    vm = it
                },

                onBeforeCredentialsCheck = {
                    vm?.preHandleIntent(it, intent) ?: false
                },

                onLaunched = {
                    vm?.handleIntent(intent)
                }
            )
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