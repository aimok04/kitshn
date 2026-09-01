package de.kitshn.language

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

actual fun preferredLanguageTags(): List<String> =
    NSLocale.preferredLanguages.filterIsInstance<String>().normalizeLanguageTags()
