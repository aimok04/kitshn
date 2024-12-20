package de.kitshn

enum class Platforms(
    val displayName: String
) {
    ANDROID("Android"),
    IOS("iOS"),
    JVM("Desktop")
}

class PlatformDetails(
    val platform: Platforms,
    val packageName: String,
    val packageVersion: String,
    val packageExtendedVersion: String,
    val buildType: String,
    val debug: Boolean
)

expect val platformDetails: PlatformDetails