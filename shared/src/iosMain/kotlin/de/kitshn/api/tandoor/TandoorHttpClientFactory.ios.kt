package de.kitshn.api.tandoor

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.ChallengeHandler
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.interpretObjCPointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFArrayRef
import platform.CoreFoundation.CFArrayRefVar
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFStringRef
import platform.Foundation.NSArray
import platform.Foundation.NSData
import platform.Foundation.NSDictionary
import platform.Foundation.NSMutableArray
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSString
import platform.Foundation.NSURLAuthenticationChallenge
import platform.Foundation.NSURLAuthenticationMethodClientCertificate
import platform.Foundation.NSURLAuthenticationMethodServerTrust
import platform.Foundation.NSURLCredential
import platform.Foundation.NSURLCredentialPersistence
import platform.Foundation.NSURLSessionAuthChallengeCancelAuthenticationChallenge
import platform.Foundation.NSURLSessionAuthChallengeDisposition
import platform.Foundation.NSURLSessionAuthChallengePerformDefaultHandling
import platform.Foundation.NSURLSessionAuthChallengeUseCredential
import platform.Foundation.credentialForTrust
import platform.Foundation.credentialWithIdentity
import platform.Foundation.dataWithBytes
import platform.Foundation.serverTrust
import platform.Security.SecCertificateRef
import platform.Security.SecIdentityRef
import platform.Security.SecPKCS12Import
import platform.Security.SecTrustEvaluateWithError
import platform.Security.SecTrustSetAnchorCertificates
import platform.Security.SecTrustSetAnchorCertificatesOnly
import platform.Security.errSecSuccess
import platform.Security.kSecImportExportPassphrase
import platform.Security.kSecImportItemCertChain
import platform.Security.kSecImportItemIdentity
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val TAG = "TandoorHttpClientFactory"

/**
 * Identity + extra trusted ca's the app will use as well
 */
@OptIn(ExperimentalForeignApi::class)
private class IosCertificateBundle(
    val identity: SecIdentityRef,
    val extraCAs: List<SecCertificateRef>,
)

@OptIn(ExperimentalEncodingApi::class, ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun createTandoorHttpClient(
    credentials: TandoorCredentials,
    onCertificateRequested: () -> Unit,
): HttpClient {
    val bundle = loadIosClientCertBundle(credentials)

    return HttpClient(Darwin) {
        followRedirects = true
        engine {
            handleChallenge(buildChallengeHandler(bundle, onCertificateRequested))
        }
    }
}

private typealias ChallengeCompletion =
        (NSURLSessionAuthChallengeDisposition, NSURLCredential?) -> Unit
private fun ChallengeCompletion.cancel() =
    invoke(NSURLSessionAuthChallengeCancelAuthenticationChallenge, null)
private fun ChallengeCompletion.default() =
    invoke(NSURLSessionAuthChallengePerformDefaultHandling, null)
private fun ChallengeCompletion.useCredential(credential: NSURLCredential?) =
    invoke(NSURLSessionAuthChallengeUseCredential, credential)

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun buildChallengeHandler(
    bundle: IosCertificateBundle?,
    onCertificateRequested: () -> Unit,
): ChallengeHandler =
    handler@{ _, _, challenge, completionHandler ->
        when (challenge.protectionSpace.authenticationMethod) {
            NSURLAuthenticationMethodClientCertificate -> {
                handleClientCertificateChallenge(bundle, onCertificateRequested, challenge, completionHandler)
            }
            NSURLAuthenticationMethodServerTrust -> {
                handleServerTrustChallenge(bundle, onCertificateRequested, challenge,
                    completionHandler
                )
            }
            else -> {
                completionHandler.default()
            }
        }
    }

@OptIn(ExperimentalForeignApi::class)
private fun handleClientCertificateChallenge(
    bundle: IosCertificateBundle?,
    onCertificateRequested: () -> Unit,
    challenge: NSURLAuthenticationChallenge,
    completionHandler: ChallengeCompletion
) {
    // notify ui layer
    onCertificateRequested()

    if (bundle == null) {
        Logger.w(TAG){"Client certificate bundle requested but none provided"}
        completionHandler.cancel()
        return
    }

    val credential = NSURLCredential.credentialWithIdentity(
        identity = bundle.identity,
        certificates = null,
        persistence = NSURLCredentialPersistence.NSURLCredentialPersistenceNone,
    )
    completionHandler.useCredential(credential)
}

@OptIn(ExperimentalForeignApi::class)
private fun handleServerTrustChallenge(
    bundle: IosCertificateBundle?,
    onCertificateRequested: () -> Unit,
    challenge: NSURLAuthenticationChallenge,
    completionHandler: ChallengeCompletion
){
    val serverTrust = challenge.protectionSpace.serverTrust
    if (serverTrust == null) {
        completionHandler.default()
        return
    }

    if(bundle != null && bundle.extraCAs.isNotEmpty()){
        val anchors = NSMutableArray().apply {
            bundle.extraCAs.forEach { addObject(it as Any) }
        }

        // NSArray and CFArray share the same mem layout so this is okay but typesystem says no
        val anchorsCFArray: CFArrayRef? = interpretCPointer(anchors.objcPtr())
        SecTrustSetAnchorCertificates(serverTrust, anchorsCFArray)
        SecTrustSetAnchorCertificatesOnly(serverTrust, false)
    }

    if (SecTrustEvaluateWithError(serverTrust, null)){
        completionHandler.useCredential(
            NSURLCredential.credentialForTrust(serverTrust)
        )
    } else {
        Logger.e(TAG){ "Server trust evaluation failed"}
        completionHandler.cancel()
    }
}


@OptIn(ExperimentalEncodingApi::class, ExperimentalForeignApi::class, BetaInteropApi::class)
private fun loadIosClientCertBundle(credentials: TandoorCredentials): IosCertificateBundle? {
    val pkcs12DataBase64 = credentials.mtlsCertificateData ?: return null
    val password = credentials.mtlsCertificatePassword ?: return null

    val pkcs12Data = try {
        pkcs12DataBase64.decodeBase64ToNSData()
    } catch (e: IllegalArgumentException) {
        Logger.e(TAG, e){ "PKCS#12 data is not valid bas64"}
        return null
    }

    // kSecImportExportPassphrase is a CFStringRef which the mem layout as NSStrin
    val passphraseKey = interpretObjCPointer<NSString>(kSecImportExportPassphrase!!.rawValue)

    val options = NSMutableDictionary().apply {
        setObject(password, forKey = passphraseKey)
    }

    return memScoped {
        val itemsOut = alloc<CFArrayRefVar>()
        // same as above we need to convert from NS to CF types
        val pkcs12CFData: CFDataRef? = interpretCPointer(pkcs12Data.objcPtr())
        val optionsCFDict: CFDictionaryRef? = interpretCPointer(options.objcPtr())
        val status = SecPKCS12Import(
            pkcs12CFData,
            optionsCFDict,
            itemsOut.ptr
        )

        if (status != errSecSuccess) {
            Logger.e(TAG) { "SecPKCS12Import failed (OSStatus=$status)"}
            return@memScoped null
        }

        val itemsRef = itemsOut.value ?: run {
            Logger.e(TAG) { "SecPKCS12Import returned a null items array" }
            return@memScoped null
        }
        // same as above we need to convert from CF to NS
        val items: NSArray = interpretObjCPointer(itemsRef.rawValue)
        if (items.count == 0uL){
            Logger.e(TAG) { "SecPKCS12Import returned 0 items" }
            return@memScoped null
        }

        val identityItem = items.objectAtIndex(0u) as? NSDictionary ?: run {
            Logger.e(TAG) { "PKCS#12 identity item is not a dictionary"}
            return@memScoped null
        }

        val identity = identityItem.cfValue<SecIdentityRef>(kSecImportItemIdentity) ?: run {
            Logger.e(TAG) { "PKCS#12 contained no identity" }
            return@memScoped null
        }

        val chain = identityItem.cfArray<SecCertificateRef>(kSecImportItemCertChain)

        IosCertificateBundle(identity, chain)
    }
}

@OptIn(ExperimentalEncodingApi::class, ExperimentalForeignApi::class, BetaInteropApi::class)
private fun String.decodeBase64ToNSData(): NSData {
    val bytes = Base64.decode(this)
    if (bytes.isEmpty()) return NSData.dataWithBytes(null, 0u)
    return bytes.usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong())
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Suppress("UNCHECKED_CAST")
private inline fun <reified T : Any> NSDictionary.cfValue(cfKey: CFStringRef?): T? {
    val key = cfKey ?: return null
    // again NSString mem compatible with CFString
    val nsKey: NSString = interpretObjCPointer(key.rawValue)
    return objectForKey(nsKey) as? T
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Suppress("UNCHECKED_CAST")
private inline fun <reified T : Any> NSDictionary.cfArray(cfKey: CFStringRef?): List<T> {
    val key = cfKey ?: return emptyList()
    val nsKey: NSString = interpretObjCPointer(key.rawValue)
    val arr = objectForKey(nsKey) as? NSArray ?: return emptyList()
    return buildList(arr.count.toInt()) {
        for (i in 0uL until arr.count) {
            (arr.objectAtIndex(i) as? T)?.let(::add)
        }
    }
}
