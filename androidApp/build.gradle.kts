import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.android.builtInKotlin)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.aboutlibraries)
}

val kitshnVersionName: String by rootProject.extra
val kitshnVersionCode: Int by rootProject.extra
val kitshnAndroidPackageName: String by rootProject.extra
val date: String by rootProject.extra

dependencies {
    implementation(libs.ui.tooling)
    implementation(libs.androidx.activityCompose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.ktor.client.okhttp)

    implementation(libs.acra.http)
    implementation(libs.acra.dialog)

    implementation(libs.accompanist.systemuicontroller)

    implementation(libs.androidx.browser)

    implementation(libs.compose.video)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)

    implementation(libs.material)

    // COMMON

    implementation(libs.material3.adaptive.navigation.suite)
    implementation(libs.material.icons.extended)

    implementation(libs.runtime)
    implementation(libs.foundation)
    implementation(libs.components.resources)
    implementation(libs.ui.tooling.preview)
    implementation(libs.lifecycle.viewmodel.compose)

    implementation(libs.compose.material.expressive)

    implementation(libs.adaptive)
    implementation(libs.adaptive.layout)
    implementation(libs.adaptive.navigation)

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
    implementation(libs.ksoup)

    implementation(libs.coil)
    implementation(libs.coil.network.ktor)

    implementation(libs.filekit.dialogs)
    implementation(libs.filekit.dialogs.compose)

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

    implementation(libs.multiplatform.markdown.renderer)
    implementation(libs.multiplatform.markdown.renderer.m3)
    implementation(libs.multiplatform.markdown.renderer.coil3)

    implementation(libs.richeditor)

    implementation(libs.material.kolor)
    implementation(libs.compose.placeholder.material)
    implementation(libs.compose.placeholder)
    implementation(libs.compose.webview.multiplatform)

    implementation(libs.reorderable)

    implementation(libs.uri.kmp)

    implementation(projects.shared)
}

android {
    namespace = "de.kitshn"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        targetSdk = 36

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
        create("release") {
            storeFile = file("keystore.jks")
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
        }
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

            applicationIdSuffix = ".debug"
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

            signingConfig = signingConfigs["release"]
        }
    }

    buildFeatures {
        buildConfig = true
    }

    androidResources {
        generateLocaleConfig = true
    }
}

//target {
//    compilerOptions {
//        @OptIn(ExperimentalKotlinGradlePluginApi::class)
//        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
//
//        dependencies {
//            androidTestImplementation(libs.screengrab)
//            androidTestImplementation(libs.androidx.ui.test.junit4.android)
//            debugImplementation(libs.androidx.ui.test.manifest)
//        }
//
//    }
//}