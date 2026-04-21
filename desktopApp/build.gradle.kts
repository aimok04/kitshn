import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
}

val kitshnVersionName: String by rootProject.extra
val kitshnAlternateVersionName: String by rootProject.extra
val kitshnAlternateBuildVersionName: String by rootProject.extra
val kitshnDesktopPackageName: String by rootProject.extra

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(projects.shared)
    implementation(compose.desktop.currentOs)
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
                TargetFormat.Rpm
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
