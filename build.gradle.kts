import java.io.FileInputStream
import java.time.LocalDate
import java.util.Properties

plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.kmp.library).apply(false)
    alias(libs.plugins.android.builtInKotlin).apply(false)
    alias(libs.plugins.kotlinx.serialization).apply(false)
    alias(libs.plugins.buildConfig).apply(false)
    alias(libs.plugins.aboutlibraries) apply false
}

val prop by extra(Properties().apply {
    load(FileInputStream(File(rootProject.rootDir, "kitshn.properties")))
})

val date by extra(LocalDate.now().toString())

val kitshnVersionName by extra("2.0.9")
val kitshnVersionCode by extra(20090)

val kitshnAlternateVersionName by extra("2.0.9")
val kitshnAlternateBuildVersionName by extra(kitshnAlternateVersionName.split(".").run {
    this[0] + "." + this[1] + "." + kitshnVersionCode
})

val kitshnAndroidPackageName by extra("de.kitshn.android")
val kitshnDesktopPackageName by extra("kitshn")

val kitshnIsBeta by extra(false)
