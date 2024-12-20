package de.kitshn

actual val platformDetails: PlatformDetails = PlatformDetails(
    platform = Platforms.ANDROID,
    packageName = BuildConfig.APPLICATION_ID,
    packageVersion = BuildConfig.VERSION_NAME,
    packageExtendedVersion = "${ BuildConfig.VERSION_NAME } (${ BuildConfig.VERSION_CODE })",
    buildType = BuildConfig.BUILD_TYPE,
    debug = BuildConfig.DEBUG
)