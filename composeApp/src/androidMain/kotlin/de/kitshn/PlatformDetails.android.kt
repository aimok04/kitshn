package de.kitshn

import kitshn.composeApp.BuildConfig

actual val platformDetails: PlatformDetails = PlatformDetails(
    platform = Platforms.ANDROID,
    packageName = BuildConfig.PACKAGE_ANDROID_NAME,
    packageVersion = BuildConfig.PACKAGE_VERSION_NAME,
    packageExtendedVersion = "${BuildConfig.PACKAGE_VERSION_NAME} (${BuildConfig.PACKAGE_VERSION_CODE})",
    buildType = "android", // BuildConfig.BUILD_TYPE is not easily available in library if not explicitly added
    debug = true // BuildConfig.DEBUG is also tricky in libraries
)