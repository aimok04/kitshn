import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.io.FileInputStream
import java.time.LocalDate
import java.util.Properties

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.aboutlibraries)
}

val prop =
    Properties().apply { load(FileInputStream(File(rootProject.rootDir, "kitshn.properties"))) }
val date = LocalDate.now().toString()

// Android/linux version name can contain more information
val kitshnVersionName = "1.0.0-alpha.14"
val kitshnVersionCode = 1140

// iOS, dmg and MSI are limited to [Major].[Minor].[Patch] format
val kitshnAlternateVersionName = "1.0.0"
val kitshnAlternateBuildVersionName = kitshnAlternateVersionName.split(".").run {
    this[0] + "." + this[1] + "." + kitshnVersionCode
}

val kitshnAndroidPackageName = "de.kitshn.android"
val kitshnDesktopPackageName = "de.kitshn.desktop"

kotlin {
    jvmToolchain(11)

    androidTarget()

    jvm()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "kitshn"
            isStatic = true

            binaryOption("bundleShortVersionString", kitshnAlternateVersionName)
            binaryOption("bundleVersion", "$kitshnAlternateVersionName.$kitshnVersionCode")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.material3AdaptiveNavigationSuite)
            implementation(compose.materialIconsExtended)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.kermit)
            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.client.logging)

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.navigation.composee)

            implementation(libs.kotlinx.serialization.json)

            implementation(libs.coil)
            implementation(libs.coil.network.ktor)

            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.multiplatform.settings.coroutines)
            implementation(libs.multiplatform.settings.make.observable)
            implementation(libs.kstore)

            implementation(libs.kotlinx.datetime)
            implementation(libs.human.readable)

            implementation(libs.haze)

            implementation(libs.aboutlibraries.core)
            implementation(libs.aboutlibraries.compose.m3)

            implementation(libs.compose.placeholder.material)
            implementation(libs.compose.placeholder)
            implementation(libs.compose.webview.multiplatform)

            implementation(libs.reorderable)

            implementation(libs.uri.kmp)
        }

        androidMain.dependencies {
            implementation(compose.uiTooling)
            implementation(libs.androidx.activityCompose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)

            implementation(libs.androidx.animation.graphics.android)
            implementation(libs.androidx.adaptive)
            implementation(libs.androidx.adaptive.layout)
            implementation(libs.androidx.adaptive.navigation)

            implementation(libs.richtext.ui.material3)
            implementation(libs.richtext.commonmark)

            implementation(libs.acra.http)
            implementation(libs.acra.dialog)

            implementation(libs.accompanist.systemuicontroller)

            implementation(libs.androidx.browser)

            implementation(libs.compose.video)
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.androidx.media3.session)
            implementation(libs.androidx.media3.ui)

            implementation(libs.material)

            implementation(libs.peekaboo.image.picker)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.okhttp)

            implementation(libs.richtext.ui.material3)
            implementation(libs.richtext.commonmark)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)

            implementation(libs.peekaboo.image.picker)
        }
    }
}

android {
    namespace = "de.kitshn"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        targetSdk = 35

        applicationId = kitshnAndroidPackageName

        versionName = kitshnVersionName
        versionCode = kitshnVersionCode

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }

    signingConfigs {
        create("nightly") {
            storeFile = file("keystore.jks")
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            manifestPlaceholders["appIcon"] = "@mipmap/ic_debug_launcher"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_debug_launcher_round"

            applicationIdSuffix = ".debug.kmp"
        }
        create("nightly") {
            manifestPlaceholders["appIcon"] = "@mipmap/ic_nightly_launcher"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_nightly_launcher_round"

            applicationIdSuffix = ".nightly"
            versionNameSuffix = "-nightly-$date"

            signingConfig = signingConfigs["nightly"]
        }
        release {
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher_round"

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    androidResources {
        generateLocaleConfig = true
    }
}

aboutLibraries {
    registerAndroidTasks = false
    excludeFields = arrayOf("generated")
}

dependencies {
    implementation(libs.androidx.ui.android)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Deb,
                TargetFormat.AppImage
            )
            packageName = kitshnDesktopPackageName
            packageVersion = kitshnVersionName

            linux {
                iconFile.set(project.file("desktopAppIcons/LinuxIcon.png"))
            }
            windows {
                iconFile.set(project.file("desktopAppIcons/WindowsIcon.ico"))

                msiPackageVersion = kitshnAlternateVersionName
            }
            macOS {
                iconFile.set(project.file("desktopAppIcons/MacosIcon.icns"))
                bundleID = kitshnDesktopPackageName

                dmgPackageVersion = kitshnAlternateVersionName
                dmgPackageBuildVersion = kitshnAlternateBuildVersionName
            }
        }
    }
}

buildConfig {
    // build properties
    buildConfigField("PACKAGE_VERSION_NAME", kitshnVersionName)
    buildConfigField("PACKAGE_VERSION_CODE", kitshnVersionCode)
    buildConfigField("PACKAGE_ALTERNATE_VERSION_NAME", kitshnAlternateVersionName)
    buildConfigField("PACKAGE_ALTERNATE_BUILD_VERSION_NAME", kitshnAlternateBuildVersionName)

    buildConfigField("PACKAGE_ANDROID_NAME", kitshnAndroidPackageName)
    buildConfigField("PACKAGE_DESKTOP_NAME", kitshnDesktopPackageName)

    // kitshn.properties
    buildConfigField("ABOUT_GITHUB", prop.getProperty("about.github"))
    buildConfigField("ABOUT_GITHUB_NEW_ISSUE", prop.getProperty("about.github.new.issue"))
    buildConfigField("ABOUT_CONTACT_WEBSITE", prop.getProperty("about.contact.website"))
    buildConfigField("ABOUT_CONTACT_MAILTO", prop.getProperty("about.contact.mailto"))

    buildConfigField("ACRA_HTTP_URI", prop.getProperty("acra.http.uri"))
    buildConfigField("ACRA_HTTP_BASIC_AUTH_LOGIN", prop.getProperty("acra.http.basic.auth.login"))
    buildConfigField(
        "ACRA_HTTP_BASIC_AUTH_PASSWORD",
        prop.getProperty("acra.http.basic.auth.password")
    )

    buildConfigField("SHARE_WRAPPER_URL", prop.getProperty("share.wrapper.url"))
}