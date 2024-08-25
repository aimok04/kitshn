import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.aboutlibraries)
}

val prop = Properties().apply {
    load(FileInputStream(File(rootProject.rootDir, "kitshn.properties")))
}

android {
    namespace = "de.kitshn.android"
    compileSdk = 35

    androidResources {
        generateLocaleConfig = true
    }

    defaultConfig {
        applicationId = "de.kitshn.android"
        minSdk = 24
        targetSdk = 35
        versionCode = 104
        versionName = "1.0.0-alpha.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        resValue("string", "about_github", prop.getProperty("about.github"))
        resValue("string", "about_github_new_issue", prop.getProperty("about.github.new.issue"))
        resValue("string", "about_contact_mailto", prop.getProperty("about.contact.mailto"))

        resValue("string", "acra_http_uri", prop.getProperty("acra.http.uri"))
        resValue(
            "string",
            "acra_http_basic_auth_login",
            prop.getProperty("acra.http.basic.auth.login")
        )
        resValue(
            "string",
            "acra_http_basic_auth_password",
            prop.getProperty("acra.http.basic.auth.password")
        )

        resValue("string", "share_wrapper_url", prop.getProperty("share.wrapper.url"))
    }

    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }

    aboutLibraries {
        excludeFields = arrayOf("generated")
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.animation.graphics.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.core.android)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.adaptive)
    implementation(libs.androidx.adaptive.layout)
    implementation(libs.androidx.adaptive.navigation)
    implementation(libs.androidx.material3.adaptive.navigation.suite.android)

    implementation(libs.material)

    implementation(libs.haze)
    implementation(libs.compose.markdown)

    implementation(libs.accompanist.placeholder.material)
    implementation(libs.accompanist.placeholder)

    implementation(libs.modernstorage.photopicker)

    implementation(libs.reorderable)

    implementation(libs.coil)
    implementation(libs.coil.compose)

    implementation(libs.volley)
    implementation(libs.androidx.browser)

    implementation(libs.acra.http)
    implementation(libs.acra.dialog)

    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.compose.m3)
}