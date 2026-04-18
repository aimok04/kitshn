package de.kitshn

import de.kitshn.api.tandoor.TandoorCredentials
import kotlinx.coroutines.flow.Flow

/**
 * Platform-specific encrypted credential storage.
 *
 * Android: EncryptedSharedPreferences (AES-256-GCM, key in Android Keystore)
 * iOS: Keychain
 * JVM: AES-encrypted file with key in Java KeyStore
 */
expect class SecureCredentialStore() {
    fun getCredentials(): Flow<TandoorCredentials?>
    fun saveCredentials(credentials: TandoorCredentials?)
}
