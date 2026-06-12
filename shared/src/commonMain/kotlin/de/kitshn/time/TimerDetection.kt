package de.kitshn.time

data class TimerDetectionDefs(
    val hourDefs: Set<String>,
    val andDefs: Set<String>,
    val minuteDefs: Set<String>,
    val secondDefs: Set<String>,
    val rangeDefs: Set<String>,
    val rangeQualifierDefs: Set<String> = emptySet()
)

private fun parseToSeconds(value: String, multiplier: Double): Int =
    ((value.replace(',', '.').toDoubleOrNull() ?: 0.0) * multiplier).toInt()

private fun Set<String>.toRegexAlt(): String =
    sortedByDescending(String::length)
        .joinToString("|") { Regex.escape(it) }

fun detectTimers(markdown: String, defs: TimerDetectionDefs): String {

    val hours = defs.hourDefs.toRegexAlt()
    val minutes = defs.minuteDefs.toRegexAlt()
    val seconds = defs.secondDefs.toRegexAlt()
    val andWords = defs.andDefs.toRegexAlt()
    val number = """[0-9]+(?:[.,][0-9]+)?"""
    val unit = "$hours|$minutes|$seconds"

    val qualifier = defs.rangeQualifierDefs
        .takeIf(Set<String>::isNotEmpty)
        ?.toRegexAlt()
        ?.let { """(?:(?:$it)\s+)?""" }
        .orEmpty()

    val rangeSep = if (defs.rangeDefs.isNotEmpty()) {
        val rangeWords = defs.rangeDefs.toRegexAlt()
        """(?:\s*-\s*|\s+(?:$rangeWords)\s+$qualifier)"""
    } else {
        """\s*-\s*"""
    }

    val hourRegex = Regex("^($hours)$", RegexOption.IGNORE_CASE)
    val minuteRegex = Regex("^($minutes)$", RegexOption.IGNORE_CASE)

    fun unitMultiplier(u: String): Double = when {
        hourRegex.matches(u) -> 3600.0
        minuteRegex.matches(u) -> 60.0
        else -> 1.0
    }

    // COMMENTS mode ignores literal whitespace/newlines so the pattern can be formatted.
    val pattern = Regex(
        """
        # Cross-unit range: "15 Sekunden bis 45 Minuten"
        (?<crossFrom>$number)\s*(?<crossFromUnit>$unit)(?!\w)
        $rangeSep
        (?<crossTo>$number)\s*(?<crossToUnit>$unit)(?!\w)

        | # Same-unit range: "10-15 min" / "0.5 bis maximal 2,3h"
        (?<sameFrom>$number)
        $rangeSep
        (?<sameTo>$number)\s*(?<sameUnit>$unit)(?!\w)

        | # Hour + minute combo: "1h 30min" / "1 Stunde und 30 Minuten"
        (?:(?<comboHours>$number)\s*(?:$hours)(?!\w)\s*(?:$andWords)?\s*)?
        (?<comboMinutes>$number)\s*(?:$minutes)(?!\w)

        | # Hours only: "2h" / "1 Stunde"
        (?<hoursOnly>$number)\s*(?:$hours)(?!\w)

        | # Seconds only: "45s" / "30 Sekündchen"
        (?<secondsOnly>$number)\s*(?:$seconds)(?!\w)
        """,
        setOf(RegexOption.IGNORE_CASE, RegexOption.COMMENTS)
    )

    return markdown.replace(pattern) { m ->
        fun named(n: String) = m.groups[n]?.value.orEmpty()
        fun timer(s: Int) = "[**⏲ ${m.value}**](timer://$s)"
        fun range(from: Int, to: Int) = "[**⏲ ${m.value}**](timer-range://$from/$to)"

        when {
            named("crossFrom").isNotBlank() -> range(
                parseToSeconds(named("crossFrom"), unitMultiplier(named("crossFromUnit"))),
                parseToSeconds(named("crossTo"), unitMultiplier(named("crossToUnit")))
            )
            named("sameFrom").isNotBlank() -> {
                val mult = unitMultiplier(named("sameUnit"))
                range(parseToSeconds(named("sameFrom"), mult), parseToSeconds(named("sameTo"), mult))
            }
            named("hoursOnly").isNotBlank() ->
                timer(parseToSeconds(named("hoursOnly"), 3600.0))
            named("secondsOnly").isNotBlank() ->
                timer(parseToSeconds(named("secondsOnly"), 1.0))
            else -> timer(
                parseToSeconds(named("comboHours").ifBlank { "0" }, 3600.0) +
                    parseToSeconds(named("comboMinutes").ifBlank { "0" }, 60.0)
            )
        }
    }
}
