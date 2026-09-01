package de.kitshn.language

import android.content.res.Resources
import android.os.LocaleList

actual fun preferredLanguageTags(): List<String> {
    val locales = buildList {
        // The app's adjusted list first, then the full system list from device settings.
        listOf(LocaleList.getDefault(), Resources.getSystem().configuration.locales)
            .forEach { list -> repeat(list.size()) { add(list[it].language) } }
    }

    return locales.normalizeLanguageTags()
}
