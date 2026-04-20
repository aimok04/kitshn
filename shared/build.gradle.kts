import com.android.build.gradle.internal.tasks.CompileArtProfileTask
import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.Properties

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.aboutlibraries)
}

val prop: Properties by rootProject.extra
val kitshnVersionName: String by rootProject.extra
val kitshnVersionCode: Int by rootProject.extra
val kitshnAlternateVersionName: String by rootProject.extra
val kitshnAlternateBuildVersionName: String by rootProject.extra
val kitshnAndroidPackageName: String by rootProject.extra
val kitshnDesktopPackageName: String by rootProject.extra
val kitshnIsBeta: Boolean by rootProject.extra

kotlin {
    jvmToolchain(17)

    android {
        namespace = "de.kitshn.shared"
        compileSdk = 36
        minSdk = 26

        androidResources.enable = true

        optimization {
            consumerKeepRules.apply {
                publish = true
                file("proguard-rules.pro")
            }
        }
    }

    jvm()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "Shared"
            isStatic = true

            binaryOption("bundleShortVersionString", kitshnAlternateVersionName)
            binaryOption("bundleVersion", "$kitshnAlternateVersionName.$kitshnVersionCode")

            export("co.touchlab.crashkios:bugsnag:0.9.0")
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val mobileMain by creating { dependsOn(commonMain.get()) }

        commonMain.dependencies {
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
        }

        androidMain {
            dependsOn(mobileMain)

            dependencies {
                implementation(libs.androidx.activityCompose)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.ktor.client.okhttp)

                implementation(libs.acra.http)
                implementation(libs.acra.dialog)

                implementation(libs.accompanist.systemuicontroller)

                implementation(libs.androidx.browser)

                implementation(libs.androidx.ui.android)
                implementation(libs.compose.video)
                implementation(libs.androidx.media3.exoplayer)
                implementation(libs.androidx.media3.session)
                implementation(libs.androidx.media3.ui)

                implementation(libs.material)
            }
        }

        iosMain {
            dependsOn(mobileMain)

            dependencies {
                implementation(libs.ktor.client.darwin)

                implementation(libs.kermit.bugsnag)
                api(libs.bugsnag)
            }
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.okhttp)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

aboutLibraries {
    export.excludeFields.add("generated")
}

dependencies {
    androidRuntimeClasspath(libs.ui.tooling)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            val formats = listOfNotNull(
                // https://github.com/JetBrains/compose-multiplatform/issues/3814
                TargetFormat.AppImage.takeUnless { Os.isFamily(Os.FAMILY_MAC) },
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Deb,
            ).toTypedArray()
            targetFormats(*formats)

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
    buildConfigField("VERSION_NAME", kitshnVersionName)
    buildConfigField("VERSION_CODE", kitshnVersionCode)

    buildConfigField("PACKAGE_VERSION_NAME", kitshnVersionName)
    buildConfigField("PACKAGE_VERSION_CODE", kitshnVersionCode)
    buildConfigField("PACKAGE_ALTERNATE_VERSION_NAME", kitshnAlternateVersionName)
    buildConfigField("PACKAGE_ALTERNATE_BUILD_VERSION_NAME", kitshnAlternateBuildVersionName)

    buildConfigField("PACKAGE_ANDROID_NAME", kitshnAndroidPackageName)
    buildConfigField("PACKAGE_DESKTOP_NAME", kitshnDesktopPackageName)

    buildConfigField("PACKAGE_IS_BETA", kitshnIsBeta)

    // kitshn.properties
    buildConfigField("ABOUT_GITHUB", prop.getProperty("about.github"))
    buildConfigField("ABOUT_GITHUB_NEW_ISSUE", prop.getProperty("about.github.new.issue"))
    buildConfigField("ABOUT_CONTACT_WEBSITE", prop.getProperty("about.contact.website"))
    buildConfigField("ABOUT_CONTACT_MAILTO", prop.getProperty("about.contact.mailto"))
    buildConfigField("ABOUT_APPLE_APPSTORE", prop.getProperty("about.apple.appstore"))

    buildConfigField("ACRA_HTTP_URI", prop.getProperty("acra.http.uri"))
    buildConfigField("ACRA_HTTP_BASIC_AUTH_LOGIN", prop.getProperty("acra.http.basic.auth.login"))
    buildConfigField(
        "ACRA_HTTP_BASIC_AUTH_PASSWORD",
        prop.getProperty("acra.http.basic.auth.password")
    )

    buildConfigField("SHARE_WRAPPER_URL", prop.getProperty("share.wrapper.url"))

    buildConfigField("FUNDING_API", prop.getProperty("funding.api"))
    buildConfigField("FUNDING_KOFI", prop.getProperty("funding.kofi"))

    buildConfigField("IOS_TIMER_SHORTCUT_LINK", prop.getProperty("ios.timer.shortcut.link"))
    buildConfigField(
        "IOS_TIMER_SHORTCUT_NAME",
        prop.getProperty("ios.timer.shortcut.name").replace("--", "—")
    )

    buildConfigField("TEST_DEMO_URL", prop.getProperty("test.demo.url"))
}

// fix for F-Droid
tasks.withType<CompileArtProfileTask> {
    enabled = false
}