package de.kitshn.time

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimerDetectionTest {

    private val defs = TimerDetectionDefs(
        hourDefs = setOf(
            "h", "hr", "hrs", "hour", "hours",
            "stündchen", "stunden", "stunde", "std.", "std"
        ),
        andDefs = setOf("and", "und"),
        minuteDefs = setOf(
            "min", "mins", "minute", "minutes",
            "minütchen", "minuten", "min."
        ),
        secondDefs = setOf(
            "s", "sec", "secs", "second", "seconds",
            "sekündchen", "sekunden", "sekunde", "sek.", "sek"
        ),
        rangeDefs = setOf("to", "bis"),
        rangeQualifierDefs = setOf(
            "maximal", "höchstens", "mindestens", "ungefähr", "etwa", "ca.",
            "about", "around", "approx", "approximately", "at most", "at least", "up to"
        )
    )

    private fun String.timerUris(withDefs: TimerDetectionDefs = defs): List<String> =
        Regex("""\(timer[^)]+\)""").findAll(detectTimers(this, withDefs)).map { it.value }.toList()

    private fun String.singleUri() = timerUris().also {
        assertEquals(1, it.size, "Expected exactly one timer in: \"$this\", got: $it")
    }.first()

    // -- SIMPLE

    @Test fun intMinutes() = assertEquals("(timer://900)", "15 min kochen".singleUri())

    @Test fun decimalCommaMinutes() = assertEquals("(timer://150)", "2,5 Min rühren".singleUri())

    @Test fun decimalDotMinutes() = assertEquals("(timer://150)", "2.5 min warten".singleUri())

    @Test fun germanMinuteFullWord() = assertEquals("(timer://1800)", "30 Minuten".singleUri())

    @Test fun germanDiminutiveMinuetchen() = assertEquals("(timer://120)", "2 Minütchen".singleUri())

    @Test fun hoursOnlyGerman() = assertEquals("(timer://7200)", "2 Stunden".singleUri())

    @Test fun hoursOnlyHrs() = assertEquals("(timer://7200)", "2 hrs".singleUri())

    @Test fun hoursOnlyH() = assertEquals("(timer://3600)", "1h".singleUri())

    @Test fun decimalHours() = assertEquals("(timer://5400)", "1,5h".singleUri())

    @Test fun germanStd() = assertEquals("(timer://3600)", "1 Std.".singleUri())

    @Test fun germanDiminutiveStuendchen() = assertEquals("(timer://3600)", "1 Stündchen".singleUri())

    @Test fun intSeconds() = assertEquals("(timer://45)", "45 sec".singleUri())

    @Test fun fullWordSeconds() = assertEquals("(timer://30)", "30 seconds".singleUri())

    @Test fun germanSekWithDot() = assertEquals("(timer://45)", "45 Sek. stehen lassen".singleUri())

    @Test fun germanDiminutiveSekuendchen() = assertEquals("(timer://30)", "30 Sekündchen".singleUri())

    @Test fun decimalSeconds() = assertEquals("(timer://3)", "2.5 s".singleUri())

    // -- Combos

    @Test fun hoursAndMinutes() = assertEquals("(timer://5400)", "1 Stunde 30 Minuten".singleUri())

    @Test fun hoursUndMinutes() = assertEquals("(timer://5400)", "1 Stunde und 30 Minuten".singleUri())

    @Test fun hoursAndMinutesEnglish() = assertEquals("(timer://5400)", "1 hour and 30 minutes".singleUri())

    @Test fun compactHMin() = assertEquals("(timer://5400)", "1h 30min".singleUri())

    @Test fun stdDotMinDot() = assertEquals("(timer://4800)", "1 Std. 20 Min.".singleUri())

    // -- ranges

    @Test fun dashRangeMinutes() = assertEquals("(timer-range://600/900)", "10-15 min".singleUri())

    @Test fun spacedDashRangeMinutes() = assertEquals("(timer-range://600/900)", "10 - 15 min".singleUri())

    @Test fun wordRangeBis() = assertEquals("(timer-range://600/900)", "10 bis 15 Minuten".singleUri())

    @Test fun wordRangeTo() = assertEquals("(timer-range://600/900)", "10 to 15 min".singleUri())

    @Test fun dashRangeSeconds() = assertEquals("(timer-range://15/24)", "15-23.5 sec".singleUri())

    @Test fun dashRangeHours() = assertEquals("(timer-range://3600/7200)", "1-2 Stunden".singleUri())

    @Test fun dashSpecialMinutes() = assertEquals("(timer-range://600/900)", "10–15 Min.".singleUri())

    @Test fun decimalCommaRangeMinutes() = assertEquals("(timer-range://90/150)", "1,5-2,5 min".singleUri())

    @Test fun decimalMixedCommaRangeMinutesWithDot() = assertEquals("(timer-range://90/150)", "1,5-2.5 min.".singleUri())

    @Test fun qualifierMaximal() =
        assertEquals("(timer-range://600/900)", "10 bis maximal 15 Minuten".singleUri())

    @Test fun qualifierMindestens() =
        assertEquals("(timer-range://600/900)", "10 bis mindestens 15 Minuten".singleUri())

    @Test fun qualifierEtwa() =
        assertEquals("(timer-range://600/900)", "10 bis etwa 15 Minuten".singleUri())

    @Test fun qualifierAbout() =
        assertEquals("(timer-range://600/900)", "10 to about 15 min".singleUri())

    @Test fun qualifierAtMost() =
        assertEquals("(timer-range://600/900)", "10 to at most 15 min".singleUri())

    @Test fun qualifierUpTo() =
        assertEquals("(timer-range://600/900)", "10 to up to 15 min".singleUri())

    @Test fun qualifierUpToDecimal() =
        assertEquals("(timer-range://600/930)", "10 to up to 15.5 min".singleUri())

    @Test fun crossUnitRangeSecondsToMinutes() =
        assertEquals("(timer-range://15/2700)", "15 Sekunden bis 45 Minuten nochmal".singleUri())

    @Test fun sameUnitRangeWithDecimalHoursAndQualifier() =
        assertEquals("(timer-range://1800/8280)", "0.5 bis maximal 2,3h kochen lassen".singleUri())

    @Test fun crossUnitAndSingleInOneSentence() {
        val uris = "0.5 bis maximal 2,3h kochen lassen und dann 15 Sekunden bis 45 Minuten nochmal".timerUris()
        assertEquals(2, uris.size)
        assertTrue("(timer-range://1800/8280)" in uris)
        assertTrue("(timer-range://15/2700)" in uris)
    }

    // -- multiple

    @Test fun multipleTimers() {
        val uris = "15 min kochen dann 30 sec warten".timerUris()
        assertEquals(2, uris.size)
        assertTrue("(timer://900)" in uris)
        assertTrue("(timer://30)" in uris)
    }

    // -- no false positive

    @Test fun noMatchPlainText() = assertTrue("Guten Morgen, schöner Tag!".timerUris().isEmpty())

    @Test fun noMatchNumberAlone() = assertTrue("Nimm 5 Äpfel".timerUris().isEmpty())

    @Test fun noMatchNumberAloneMisleadingSeconds() = assertTrue("Die 2 Sekundarstufe :D".timerUris().isEmpty())

    // -- dash variants

    @Test fun emDashRangeMinutes() = assertEquals("(timer-range://600/900)", "10\u201415 min".singleUri())

    @Test fun figureDashRangeMinutes() = assertEquals("(timer-range://600/900)", "10\u201215 min".singleUri())

    @Test fun horizontalBarRangeMinutes() = assertEquals("(timer-range://600/900)", "10\u201515 min".singleUri())

    @Test fun nonBreakingHyphenRangeMinutes() = assertEquals("(timer-range://600/900)", "10\u201115 min".singleUri())

    @Test fun minusSignRangeMinutes() = assertEquals("(timer-range://600/900)", "10\u221215 min".singleUri())

    @Test fun fullwidthHyphenRangeMinutes() = assertEquals("(timer-range://600/900)", "10\uFF0D15 min".singleUri())

    @Test fun waveDashRangeMinutes() = assertEquals("(timer-range://600/900)", "10\u301C15 min".singleUri())

    @Test fun spacedEmDashRangeHours() = assertEquals("(timer-range://3600/7200)", "1 \u2014 2 Stunden".singleUri())

    // -- non-latin vocabularies

    private val cyrillicDefs = TimerDetectionDefs(
        hourDefs = setOf("\u0447\u0430\u0441\u043E\u0432", "\u0447\u0430\u0441\u0430", "\u0447"),
        andDefs = setOf("\u0438"),
        minuteDefs = setOf("\u043C\u0438\u043D\u0443\u0442", "\u043C\u0438\u043D"),
        secondDefs = setOf("\u0441\u0435\u043A\u0443\u043D\u0434", "\u0441\u0435\u043A"),
        rangeDefs = setOf("\u0434\u043E")
    )

    private fun String.cyrillicUris(): List<String> =
        Regex("""\(timer[^)]+\)""").findAll(detectTimers(this, cyrillicDefs)).map { it.value }.toList()

    @Test fun cyrillicMinutes() =
        assertEquals(listOf("(timer://900)"), "\u0432\u0430\u0440\u0438\u0442\u044C 15 \u043C\u0438\u043D\u0443\u0442".cyrillicUris())

    @Test fun cyrillicDashRange() =
        assertEquals(listOf("(timer-range://600/900)"), "10\u201315 \u043C\u0438\u043D".cyrillicUris())

    /** "\u043C\u0438\u043D" is a prefix of "\u043C\u0438\u043D\u0434\u0430\u043B\u044C" (almond); the ASCII-only (?!\w) guard cannot catch that. */
    @Test fun cyrillicNoMatchInsideLongerWord() =
        assertTrue("\u0434\u043E\u0431\u0430\u0432\u044C 5 \u043C\u0438\u043D\u0434\u0430\u043B\u044C".cyrillicUris().isEmpty())

    // -- FRACTIONS

    @Test fun mixedAsciiFractionHours() = assertEquals("(timer://9000)", "2 1/2 hours".singleUri())

    @Test fun mixedVulgarFractionHours() = assertEquals("(timer://9000)", "2 \u00BD hours".singleUri())

    @Test fun gluedVulgarFractionHours() = assertEquals("(timer://9000)", "2\u00BD Stunden".singleUri())

    @Test fun asciiFractionOnly() = assertEquals("(timer://1800)", "1/2 hour".singleUri())

    @Test fun vulgarFractionOnly() = assertEquals("(timer://1800)", "\u00BD Stunde".singleUri())

    @Test fun vulgarFractionQuarterHour() = assertEquals("(timer://900)", "\u00BC h".singleUri())

    @Test fun thirdOfAnHourRoundsCleanly() = assertEquals("(timer://1200)", "\u2153 Stunde".singleUri())

    @Test fun fractionRange() =
        assertEquals("(timer-range://5400/7200)", "1 1/2 bis 2 Stunden".singleUri())

    @Test fun fractionKeepsWholeFigureInLinkText() = assertEquals(
        "[**\u23F2 2 1/2 hours**](timer://9000)",
        detectTimers("2 1/2 hours", defs)
    )

    /** A fraction without a unit is an amount, not a timer. */
    @Test fun fractionWithoutUnitIsNoTimer() =
        assertTrue("add 1/2 cup sugar and 2 \u00BD cups flour".timerUris().isEmpty())

    // -- vocabulary resolution

    @Test fun defsAlwaysIncludeEnglishAndUnitSymbols() {
        val defs = timerDetectionDefs(activeLanguage = "fr", allInstalledLanguages = false)
        assertTrue("minutes" in defs.minuteDefs)
        assertTrue("min" in defs.minuteDefs)
        assertTrue("h" in defs.hourDefs)
    }

    @Test fun defsMergeActiveLanguageWithEnglish() {
        val defs = timerDetectionDefs(activeLanguage = "de", allInstalledLanguages = false)
        assertTrue("minuten" in defs.minuteDefs)
        assertTrue("minutes" in defs.minuteDefs)
        assertTrue("bis" in defs.rangeDefs)
        assertTrue("to" in defs.rangeDefs)
    }

    @Test fun mergedDefsDetectBothLanguages() {
        val defs = timerDetectionDefs(activeLanguage = "de", allInstalledLanguages = false)
        assertEquals("[**\u23F2 10 bis 15 Minuten**](timer-range://600/900)", detectTimers("10 bis 15 Minuten", defs))
        assertEquals("[**\u23F2 10 to 15 minutes**](timer-range://600/900)", detectTimers("10 to 15 minutes", defs))
    }

    @Test fun alternativeRangeWordsAreDetected() {
        val defs = timerDetectionDefs(activeLanguage = "de", allInstalledLanguages = false)
        assertEquals("[**\u23F2 3 or 4 minutes**](timer-range://180/240)", detectTimers("3 or 4 minutes", defs))
        assertEquals("[**\u23F2 10 until 15 minutes**](timer-range://600/900)", detectTimers("10 until 15 minutes", defs))
        assertEquals("[**\u23F2 10 till 15 min**](timer-range://600/900)", detectTimers("10 till 15 min", defs))
        assertEquals("[**\u23F2 10 oder 15 Minuten**](timer-range://600/900)", detectTimers("10 oder 15 Minuten", defs))
    }

    @Test fun ampersandJoinsHoursAndMinutes() {
        val defs = timerDetectionDefs(activeLanguage = "de", allInstalledLanguages = false)
        assertEquals("[**\u23F2 1 hour & 30 minutes**](timer://5400)", detectTimers("1 hour & 30 minutes", defs))
        assertEquals("[**\u23F2 1 Stunde & 30 Minuten**](timer://5400)", detectTimers("1 Stunde & 30 Minuten", defs))
    }

    @Test fun germanRangeQualifiersAreDetected() {
        val defs = timerDetectionDefs(activeLanguage = "de", allInstalledLanguages = false)
        assertEquals("[**\u23F2 10 bis max. 15 Minuten**](timer-range://600/900)", detectTimers("10 bis max. 15 Minuten", defs))
        assertEquals("[**\u23F2 10 bis knapp 15 Minuten**](timer-range://600/900)", detectTimers("10 bis knapp 15 Minuten", defs))
    }

    /** A merged vocabulary must not turn ordinary words into timers. */
    @Test fun mergedDefsDoNotMisreadOrdinaryWords() {
        val defs = timerDetectionDefs(activeLanguage = "de", allInstalledLanguages = false)
        assertTrue("Nimm 5 \u00C4pfel".timerUris(defs).isEmpty())
        assertTrue("Die 2 Sekundarstufe :D".timerUris(defs).isEmpty())
        assertTrue("add 3 minced garlic cloves".timerUris(defs).isEmpty())
    }
}
