package de.kitshn.language

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val MINUTES = "timer_detection_minute_definitions"
private const val TO = "timer_detection_to_definitions"

class MultiLocalizeTest {

    @Test fun languageTagsAreNormalized() = assertEquals(
        listOf("de", "en", "pt"),
        listOf("de-DE", "de_AT", "en-US.UTF-8", "", "C", "pt-BR").normalizeLanguageTags()
    )

    @Test fun activeLanguageComesFirstAndIsNormalized() = assertEquals(
        listOf("pt", "en"),
        multiLocalizeLanguages(
            activeLanguage = "pt_BR",
            allInstalledLanguages = false,
            alwaysIncluded = listOf("en")
        )
    )

    @Test fun alwaysIncludedDoesNotDuplicateActiveLanguage() = assertEquals(
        listOf("en"),
        multiLocalizeLanguages(
            activeLanguage = "en",
            allInstalledLanguages = false,
            alwaysIncluded = listOf("en")
        )
    )

    @Test fun installedLanguagesAreAppendedWhenEnabled() {
        val languages = multiLocalizeLanguages(
            activeLanguage = "de",
            allInstalledLanguages = true,
            alwaysIncluded = listOf("en")
        )

        assertEquals("de", languages.first())
        assertEquals(emptySet(), preferredLanguageTags().toSet() - languages.toSet())
        assertEquals(languages, languages.distinct())
    }

    @Test fun stringArrayCarriesEveryTranslation() {
        val minutes = localizedStringArray(MINUTES)
        assertTrue("minutes" in minutes.getValue("en"))
        assertTrue("minuten" in minutes.getValue("de"))
    }

    @Test fun unknownResourcesResolveToNothing() {
        assertEquals(emptyMap(), localizedString("nope"))
        assertEquals(emptyMap(), localizedStringArray("nope"))
        assertEquals(
            emptyList(),
            multiLocalize(listOf("nope"), activeLanguage = "de", allInstalledLanguages = false)
        )
    }

    @Test fun resourcesAreCollectedPerLanguageInRelevanceOrder() {
        val localized = multiLocalize(
            names = listOf(MINUTES, TO),
            activeLanguage = "de",
            allInstalledLanguages = false,
            alwaysIncluded = listOf("en")
        )

        assertEquals(2, localized.size)
        assertTrue("minuten" in localized.first().getValue(MINUTES))
        assertTrue("bis" in localized.first().getValue(TO))
        assertTrue("minutes" in localized.last().getValue(MINUTES))
    }

    @Test fun languagesWithoutAnyOfTheResourcesAreSkipped() = assertEquals(
        emptyList(),
        multiLocalize(listOf(MINUTES), activeLanguage = "ta", allInstalledLanguages = false)
    )
}
