package de.kitshn

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.kitshn.actions.handleIntent
import de.kitshn.actions.preHandleIntent
import de.kitshn.migration.LegacySettingsViewModel
import de.kitshn.migration.runSettingsMigration
import de.kitshn.ui.theme.KitshnTheme
import kotlinx.coroutines.delay
import java.io.File

const val KEY_SETTINGS_ANDROID_MIGRATION_DONE = "android_migration_done"

class AppActivity : ComponentActivity() {
    private var vm: KitshnViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settings = SettingsViewModel()

        enableEdgeToEdge()
        setContent {
            var androidMigrationDone by remember {
                mutableStateOf(
                    settings.settings.getBoolean(
                        KEY_SETTINGS_ANDROID_MIGRATION_DONE,
                        false
                    )
                )
            }

            if(!androidMigrationDone) {
                LaunchedEffect(Unit) {
                    delay(500)

                    val datastoreFolder = File(filesDir, "datastore")
                    if(datastoreFolder.exists()) {
                        if(datastoreFolder.listFiles()?.isEmpty() == false) {
                            // perform migration
                            val legacySettings =
                                LegacySettingsViewModel(application, this@AppActivity)
                            val finished = runSettingsMigration(
                                settings = settings,
                                legacySettings = legacySettings
                            )

                            if(!finished) return@LaunchedEffect
                        }
                    }

                    // mark Android migration as done
                    settings.settings.putBoolean(KEY_SETTINGS_ANDROID_MIGRATION_DONE, true)
                    androidMigrationDone = true

                    // delete datastore after
                    if(datastoreFolder.exists()) datastoreFolder.deleteRecursively()
                }

                KitshnTheme {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
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