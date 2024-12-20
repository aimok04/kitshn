package de.kitshn

import kitshn.composeApp.BuildConfig

actual val platformDetails: PlatformDetails = PlatformDetails(
    platform = Platforms.JVM,
    packageName = BuildConfig.PACKAGE_DESKTOP_NAME,
    packageVersion = BuildConfig.PACKAGE_VERSION_NAME + " / " + BuildConfig.PACKAGE_ALTERNATE_VERSION_NAME,
    packageExtendedVersion = "${BuildConfig.PACKAGE_VERSION_NAME} (${BuildConfig.PACKAGE_VERSION_CODE}) / ${BuildConfig.PACKAGE_ALTERNATE_VERSION_NAME} (${BuildConfig.PACKAGE_ALTERNATE_BUILD_VERSION_NAME})",
    buildType = "jvm",
    debug = false
)