package de.kitshn

import de.kitshn.api.tandoor.TandoorCredentials
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val KEY_ALIAS = "kitshn_credentials_key"
private const val KEYSTORE_FILE = "kitshn_keystore.p12"
private const val CREDENTIALS_FILE = "kitshn_credentials.enc"
private const val GCM_TAG_LENGTH = 128
private const val GCM_IV_LENGTH = 12

actual class SecureCredentialStore actual constructor() {
    private val credentialsFlow = MutableStateFlow<TandoorCredentials?>(null)
    private val storageDir = File(System.getProperty("user.home"), ".kitshn").apply { mkdirs() }

    init {
        credentialsFlow.value = readEncrypted()
    }

    actual fun getCredentials(): Flow<TandoorCredentials?> = credentialsFlow

    actual fun saveCredentials(credentials: TandoorCredentials?) {
        if (credentials == null) {
            File(storageDir, CREDENTIALS_FILE).delete()
        } else {
            writeEncrypted(json.encodeToString(credentials))
        }
        credentialsFlow.value = credentials
    }

    private fun getOrCreateKey(): SecretKey {
        val keystoreFile = File(storageDir, KEYSTORE_FILE)
        val keyStore = KeyStore.getInstance("PKCS12")
        val password = "kitshn-local".toCharArray()

        if (keystoreFile.exists()) {
            keystoreFile.inputStream().use { keyStore.load(it, password) }
            val entry = keyStore.getEntry(KEY_ALIAS, KeyStore.PasswordProtection(password))
            if (entry is KeyStore.SecretKeyEntry) {
                return entry.secretKey
            }
        } else {
            keyStore.load(null, password)
        }

        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256, SecureRandom())
        val key = keyGen.generateKey()

        keyStore.setEntry(
            KEY_ALIAS,
            KeyStore.SecretKeyEntry(key),
            KeyStore.PasswordProtection(password)
        )
        keystoreFile.outputStream().use { keyStore.store(it, password) }

        return key
    }

    private fun writeEncrypted(plaintext: String) {
        val key = getOrCreateKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(GCM_IV_LENGTH).also { SecureRandom().nextBytes(it) }
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        File(storageDir, CREDENTIALS_FILE).writeBytes(iv + encrypted)
    }

    private fun readEncrypted(): TandoorCredentials? {
        val file = File(storageDir, CREDENTIALS_FILE)
        if (!file.exists()) return null

        return try {
            val data = file.readBytes()
            if (data.size <= GCM_IV_LENGTH) return null

            val iv = data.sliceArray(0 until GCM_IV_LENGTH)
            val encrypted = data.sliceArray(GCM_IV_LENGTH until data.size)

            val key = getOrCreateKey()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))

            val decrypted = cipher.doFinal(encrypted)
            json.maybeDecodeFromString<TandoorCredentials>(decrypted.toString(Charsets.UTF_8))
        } catch (_: Exception) {
            null
        }
    }
}
