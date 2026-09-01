package de.kitshn.language

expect fun preferredLanguageTags(): List<String>

internal fun List<String>.normalizeLanguageTags(): List<String> = asSequence()
    .map { it.trim().replace('_', '-').substringBefore('-').lowercase() }
    .filter { it.isNotBlank() && it != "c" && it != "posix" }
    .distinct()
    .toList()

fun localizedString(name: String): Map<String, String> = LOCALIZED_STRINGS[name].orEmpty()

fun localizedStringArray(name: String): Map<String, List<String>> =
    LOCALIZED_STRING_ARRAYS[name].orEmpty()

fun multiLocalizeLanguages(
    activeLanguage: String,
    allInstalledLanguages: Boolean,
    alwaysIncluded: List<String> = emptyList()
): List<String> = buildList {
    add(activeLanguage)
    if(allInstalledLanguages) addAll(preferredLanguageTags())
    addAll(alwaysIncluded)
}.normalizeLanguageTags()

fun multiLocalize(
    names: List<String>,
    activeLanguage: String,
    allInstalledLanguages: Boolean,
    alwaysIncluded: List<String> = emptyList()
): List<Map<String, List<String>>> {
    val arrays = names.associateWith { localizedStringArray(it) }

    return multiLocalizeLanguages(activeLanguage, allInstalledLanguages, alwaysIncluded)
        .mapNotNull { language ->
            names.associateWith { name -> arrays.getValue(name)[language].orEmpty() }
                .takeIf { defs -> defs.values.any(List<String>::isNotEmpty) }
        }
}
