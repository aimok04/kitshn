package de.kitshn.time

import de.kitshn.language.multiLocalize

private const val HOUR_DEFINITIONS = "timer_detection_hour_definitions"
private const val AND_DEFINITIONS = "timer_detection_and_definitions"
private const val MINUTE_DEFINITIONS = "timer_detection_minute_definitions"
private const val SECOND_DEFINITIONS = "timer_detection_second_definitions"
private const val TO_DEFINITIONS = "timer_detection_to_definitions"
private const val RANGE_QUALIFIER_DEFINITIONS = "timer_detection_range_qualifier_definitions"

private val TIMER_DETECTION_DEFINITIONS = listOf(
    HOUR_DEFINITIONS,
    AND_DEFINITIONS,
    MINUTE_DEFINITIONS,
    SECOND_DEFINITIONS,
    TO_DEFINITIONS,
    RANGE_QUALIFIER_DEFINITIONS
)

private fun Map<String, List<String>>.wordsOf(name: String) = this[name].orEmpty().toSet()

fun timerDetectionDefs(
    activeLanguage: String,
    allInstalledLanguages: Boolean
): TimerDetectionDefs = (listOf(UNIVERSAL_TIMER_DETECTION_DEFS) + multiLocalize(
    names = TIMER_DETECTION_DEFINITIONS,
    activeLanguage = activeLanguage,
    allInstalledLanguages = allInstalledLanguages,
    alwaysIncluded = listOf("en")
).map { definitions ->
    TimerDetectionDefs(
        hourDefs = definitions.wordsOf(HOUR_DEFINITIONS),
        andDefs = definitions.wordsOf(AND_DEFINITIONS),
        minuteDefs = definitions.wordsOf(MINUTE_DEFINITIONS),
        secondDefs = definitions.wordsOf(SECOND_DEFINITIONS),
        rangeDefs = definitions.wordsOf(TO_DEFINITIONS),
        rangeQualifierDefs = definitions.wordsOf(RANGE_QUALIFIER_DEFINITIONS)
    )
}).mergeDefs()
