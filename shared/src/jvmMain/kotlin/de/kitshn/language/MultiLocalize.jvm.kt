package de.kitshn.language

import java.util.Locale

actual fun preferredLanguageTags(): List<String> {
    // using posix LANG env is the best we can do
    val fromEnvironment = listOf("LANGUAGE", "LC_ALL", "LC_MESSAGES", "LANG")
        .mapNotNull(System::getenv)
        .flatMap { it.split(':') }
        .map { it.substringBefore('.') }

    val fromJvm = listOf(
        Locale.getDefault(Locale.Category.DISPLAY).language,
        Locale.getDefault(Locale.Category.FORMAT).language
    )

    return (fromEnvironment + fromJvm).normalizeLanguageTags()
}
