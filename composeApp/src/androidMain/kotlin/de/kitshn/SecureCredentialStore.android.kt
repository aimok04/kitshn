package de.kitshn

import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import de.kitshn.api.tandoor.TandoorCredentials
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

private const val PREFS_FILE = "kitshn_secure_credentials"
private const val KEY_CREDENTIALS = "credentials"

actual class SecureCredentialStore actual constructor() {
    private val credentialsFlow = MutableStateFlow<TandoorCredentials?>(null)

    private val prefs: SharedPreferences by lazy {
        val context = AndroidApp.appContext
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    init {
        try {
            credentialsFlow.value = readCredentials()
        } catch (_: Exception) {
        }
    }

    private fun readCredentials(): TandoorCredentials? {
        val jsonStr = prefs.getString(KEY_CREDENTIALS, null) ?: return null
        return json.maybeDecodeFromString<TandoorCredentials>(jsonStr)
    }

    actual fun getCredentials(): Flow<TandoorCredentials?> = credentialsFlow

    actual fun saveCredentials(credentials: TandoorCredentials?) {
        if (credentials == null) {
            prefs.edit().remove(KEY_CREDENTIALS).apply()
        } else {
            prefs.edit().putString(KEY_CREDENTIALS, json.encodeToString(credentials)).apply()
        }
        credentialsFlow.value = credentials
    }
}
