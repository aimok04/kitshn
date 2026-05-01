plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.aboutlibraries)
}

val kitshnVersionName: String by rootProject.extra
val kitshnVersionCode: Int by rootProject.extra
val kitshnAndroidPackageName: String by rootProject.extra
val date: String by rootProject.extra

dependencies {
    implementation(projects.shared)
    implementation(libs.androidx.activityCompose)
    debugImplementation(libs.ui.tooling)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}

android {
    namespace = "de.kitshn"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        targetSdk = 36

        applicationId = kitshnAndroidPackageName

        versionName = kitshnVersionName
        versionCode = kitshnVersionCode

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    dependenciesInfo {
        includeInApk = false
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
