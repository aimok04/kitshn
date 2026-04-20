package de.kitshn

import platform.Foundation.NSBundle
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
actual val platformDetails: PlatformDetails = PlatformDetails(
    platform = Platforms.IOS,
    packageName = NSBundle.mainBundle().bundleIdentifier ?: "unknown",
    packageVersion = NSBundle.mainBundle().infoDictionary?.get("CFBundleShortVersionString")
        ?.toString() ?: "unknown",
    packageExtendedVersion = "${
        NSBundle.mainBundle().infoDictionary?.get("CFBundleShortVersionString")
            ?.toString() ?: "unknown"
    } (${
        NSBundle.mainBundle().infoDictionary?.get("CFBundleVersion")?.toString() ?: "unknown"
    })",
    buildType = if(Platform.isDebugBinary) "DEBUG" else "RELEASE",
    debug = Platform.isDebugBinary
)