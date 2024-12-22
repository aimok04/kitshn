package de.kitshn

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.kitshn.actions.handleIntent
import de.kitshn.actions.preHandleIntent

class AppActivity : ComponentActivity() {
    private var vm: KitshnViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

@Preview
@Composable
fun AppPreview() {
    App()
}
