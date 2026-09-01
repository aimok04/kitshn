import com.android.build.gradle.internal.tasks.CompileArtProfileTask
import java.util.Properties

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.aboutlibraries)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
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
    jvmToolchain(21)

    android {
        namespace = "de.kitshn.shared"
        compileSdk = 37
        minSdk = 24

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
        // iosX64(),
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

            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)

            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
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

                api(project.dependencies.platform(libs.koin.bom))
                api(libs.koin.android)
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

kotlin.targets.configureEach {
    if (name != "metadata") {
        val kspConfigName = "ksp${name.replaceFirstChar { it.uppercaseChar() }}"
        dependencies.add(kspConfigName, libs.androidx.room.compiler)
    }
}

room {
    schemaDirectory("$projectDir/schemas")
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

abstract class GenerateLocalizedResources : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val resourceDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val stringNames: ListProperty<String>

    @get:Input
    abstract val stringArrayNames: ListProperty<String>

    private fun languageOf(directoryName: String): String? {
        if(directoryName == "values") return "en"
        val qualifiers = directoryName.removePrefix("values-").takeIf { it != directoryName }
            ?: return null
        val language = if(qualifiers.startsWith("b+")) {
            qualifiers.removePrefix("b+").substringBefore('+')
        } else {
            qualifiers.substringBefore('-')
        }
        return language.lowercase().takeIf { it.isNotBlank() }
    }

    private fun String.matchesAny(patterns: List<String>) = patterns.any { pattern ->
        if(pattern.endsWith("*")) startsWith(pattern.dropLast(1)) else this == pattern
    }

    private fun String.escapeKotlin() = replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("$", "\\$")

    private fun org.w3c.dom.Node.childElements(tag: String): List<org.w3c.dom.Element> {
        val nodes = when(this) {
            is org.w3c.dom.Document -> getElementsByTagName(tag)
            is org.w3c.dom.Element -> getElementsByTagName(tag)
            else -> return emptyList()
        }
        return (0 until nodes.length).map { nodes.item(it) as org.w3c.dom.Element }
    }

    private fun renderMap(entries: Map<String, Map<String, String>>): String {
        if(entries.isEmpty()) return "emptyMap()"
        return entries.entries.joinToString(",\n", "mapOf(\n", "\n)") { (name, byLanguage) ->
            val languages = byLanguage.entries.joinToString(",\n") { (language, value) ->
                """        "$language" to $value"""
            }
            "    \"$name\" to mapOf(\n$languages\n    )"
        }
    }

    @TaskAction
    fun generate() {
        val builder = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()

        val strings = sortedMapOf<String, MutableMap<String, String>>()
        val arrays = sortedMapOf<String, MutableMap<String, MutableList<String>>>()

        resourceDirectory.get().asFile.listFiles()
            .orEmpty()
            .filter { it.isDirectory }
            .sortedBy { it.name }
            .forEach { directory ->
                val language = languageOf(directory.name) ?: return@forEach
                val file = directory.resolve("strings.xml").takeIf { it.isFile } ?: return@forEach
                val document = builder.parse(file)

                document.childElements("string")
                    .filter { it.getAttribute("name").matchesAny(stringNames.get()) }
                    .forEach { element ->
                        val value = element.textContent.trim().takeIf(String::isNotEmpty)
                            ?: return@forEach
                        // Locale variants of the same language (values-pt-rBR, values-pt) are
                        // merged
                        strings.getOrPut(element.getAttribute("name")) { sortedMapOf() }
                            .putIfAbsent(language, value)
                    }

                document.childElements("string-array")
                    .filter { it.getAttribute("name").matchesAny(stringArrayNames.get()) }
                    .forEach { element ->
                        val items = element.childElements("item")
                            .map { it.textContent.trim() }
                            .filter { it.isNotEmpty() }
                        if(items.isEmpty()) return@forEach

                        arrays.getOrPut(element.getAttribute("name")) { sortedMapOf() }
                            .getOrPut(language) { mutableListOf() }
                            .addAll(items)
                    }
            }

        val stringEntries = renderMap(
            strings.mapValues { (_, byLanguage) ->
                byLanguage.mapValues { (_, value) -> "\"${value.escapeKotlin()}\"" }
            }
        )

        val arrayEntries = renderMap(
            arrays.mapValues { (_, byLanguage) ->
                byLanguage.mapValues { (_, items) ->
                    items.distinct().joinToString(", ", "listOf(", ")") {
                        "\"${it.escapeKotlin()}\""
                    }
                }
            }
        )

        val output = outputDirectory.get().asFile
            .resolve(packageName.get().replace('.', '/'))
        output.mkdirs()
        output.resolve("MultiLocalizeGenerated.kt").writeText(
            """
            |// Generated by :shared:generateLocalizedResources -- do not edit.
            |// Source: shared/src/commonMain/composeResources/values*/strings.xml
            |package ${packageName.get()}
            |
            |internal val LOCALIZED_STRINGS: Map<String, Map<String, String>> = $stringEntries
            |
            |internal val LOCALIZED_STRING_ARRAYS: Map<String, Map<String, List<String>>> = $arrayEntries
            |
            """.trimMargin()
        )
    }
}

val generateLocalizedResources =
    tasks.register<GenerateLocalizedResources>("generateLocalizedResources") {
        description = "Generate locale maps for multi language sections of the App"
        resourceDirectory.set(layout.projectDirectory.dir("src/commonMain/composeResources"))
        outputDirectory.set(layout.buildDirectory.dir("generated/localizedResources/kotlin"))
        packageName.set("de.kitshn.language")
        stringNames.set(emptyList<String>()) // resources to include everywhere
        stringArrayNames.set(listOf("timer_detection_*"))
    }

kotlin.sourceSets.commonMain { kotlin.srcDir(generateLocalizedResources) }
