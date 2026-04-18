package de.kitshn

import de.kitshn.api.tandoor.TandoorCredentials
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleWhenUnlockedThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

private const val SERVICE_NAME = "de.kitshn.credentials"
private const val ACCOUNT_NAME = "tandoor_credentials"

@OptIn(ExperimentalForeignApi::class)
actual class SecureCredentialStore actual constructor() {
    private val credentialsFlow = MutableStateFlow<TandoorCredentials?>(null)

    init {
        credentialsFlow.value = readFromKeychain()
    }

    actual fun getCredentials(): Flow<TandoorCredentials?> = credentialsFlow

    actual fun saveCredentials(credentials: TandoorCredentials?) {
        if (credentials == null) {
            deleteFromKeychain()
        } else {
            writeToKeychain(json.encodeToString(credentials))
        }
        credentialsFlow.value = credentials
    }

    private fun readFromKeychain(): TandoorCredentials? {
        memScoped {
            val query = CFDictionaryCreateMutable(kCFAllocatorDefault, 5, null, null)
            CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
            CFDictionaryAddValue(query, kSecAttrService, CFBridgingRetain(SERVICE_NAME))
            CFDictionaryAddValue(query, kSecAttrAccount, CFBridgingRetain(ACCOUNT_NAME))
            CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
            CFDictionaryAddValue(query, kSecMatchLimit, kSecMatchLimitOne)

            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query, result.ptr)

            if (status != errSecSuccess) return null

            val data = CFBridgingRelease(result.value) as? NSData ?: return null
            val jsonStr = NSString.create(data = data, encoding = NSUTF8StringEncoding)
                ?.toString() ?: return null

            return json.maybeDecodeFromString<TandoorCredentials>(jsonStr)
        }
    }

    private fun writeToKeychain(value: String) {
        deleteFromKeychain()

        memScoped {
            val nsData = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return

            val query = CFDictionaryCreateMutable(kCFAllocatorDefault, 5, null, null)
            CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
            CFDictionaryAddValue(query, kSecAttrService, CFBridgingRetain(SERVICE_NAME))
            CFDictionaryAddValue(query, kSecAttrAccount, CFBridgingRetain(ACCOUNT_NAME))
            CFDictionaryAddValue(query, kSecValueData, CFBridgingRetain(nsData))
            CFDictionaryAddValue(
                query,
                kSecAttrAccessible,
                kSecAttrAccessibleWhenUnlockedThisDeviceOnly
            )

            SecItemAdd(query, null)
        }
    }

    private fun deleteFromKeychain() {
        memScoped {
            val query = CFDictionaryCreateMutable(kCFAllocatorDefault, 3, null, null)
            CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
            CFDictionaryAddValue(query, kSecAttrService, CFBridgingRetain(SERVICE_NAME))
            CFDictionaryAddValue(query, kSecAttrAccount, CFBridgingRetain(ACCOUNT_NAME))

            SecItemDelete(query)
        }
    }
}
