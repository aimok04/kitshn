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

    private fun String.timerUris(): List<String> =
        Regex("""\(timer[^)]+\)""").findAll(detectTimers(this, defs)).map { it.value }.toList()

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

    @Test fun decimalSeconds() = assertEquals("(timer://2)", "2.5 s".singleUri())

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

    @Test fun dashRangeSeconds() = assertEquals("(timer-range://15/23)", "15-23.5 sec".singleUri())

    @Test fun dashRangeHours() = assertEquals("(timer-range://3600/7200)", "1-2 Stunden".singleUri())

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
}
