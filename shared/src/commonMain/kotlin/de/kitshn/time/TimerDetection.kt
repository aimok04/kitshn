package de.kitshn.time

import kotlin.math.roundToInt

data class TimerDetectionDefs(
    val hourDefs: Set<String>,
    val andDefs: Set<String>,
    val minuteDefs: Set<String>,
    val secondDefs: Set<String>,
    val rangeDefs: Set<String>,
    val rangeQualifierDefs: Set<String> = emptySet(),
    val numberDefs: Map<String, Double> = emptyMap()
)

/** combine languages */
operator fun TimerDetectionDefs.plus(other: TimerDetectionDefs) = TimerDetectionDefs(
    hourDefs = hourDefs + other.hourDefs,
    andDefs = andDefs + other.andDefs,
    minuteDefs = minuteDefs + other.minuteDefs,
    secondDefs = secondDefs + other.secondDefs,
    rangeDefs = rangeDefs + other.rangeDefs,
    rangeQualifierDefs = rangeQualifierDefs + other.rangeQualifierDefs,
    numberDefs = numberDefs + other.numberDefs
)

fun Iterable<TimerDetectionDefs>.mergeDefs(): TimerDetectionDefs =
    fold(EMPTY_TIMER_DETECTION_DEFS) { acc, defs -> acc + defs }

val EMPTY_TIMER_DETECTION_DEFS = TimerDetectionDefs(
    hourDefs = emptySet(),
    andDefs = emptySet(),
    minuteDefs = emptySet(),
    secondDefs = emptySet(),
    rangeDefs = emptySet()
)

val UNIVERSAL_TIMER_DETECTION_DEFS = TimerDetectionDefs(
    hourDefs = setOf("h", "hr", "hrs"),
    andDefs = emptySet(),
    minuteDefs = setOf("min", "mins"),
    secondDefs = setOf("s", "sec", "secs"),
    rangeDefs = emptySet()
)

/** We must handle whitespaces differently in ideographic languages */
private fun Char.isIdeographic(): Boolean = this in '\u3040'..'\u30FF' ||
        this in '\u3400'..'\u4DBF' || this in '\u4E00'..'\u9FFF' || this in '\uF900'..'\uFAFF'

private val COMMON_FRACTIONS = mapOf(
    '¼' to 1.0 / 4, '½' to 1.0 / 2, '¾' to 3.0 / 4,
    '⅐' to 1.0 / 7, '⅑' to 1.0 / 9, '⅒' to 1.0 / 10,
    '⅓' to 1.0 / 3, '⅔' to 2.0 / 3,
    '⅕' to 1.0 / 5, '⅖' to 2.0 / 5, '⅗' to 3.0 / 5, '⅘' to 4.0 / 5,
    '⅙' to 1.0 / 6, '⅚' to 5.0 / 6,
    '⅛' to 1.0 / 8, '⅜' to 3.0 / 8, '⅝' to 5.0 / 8, '⅞' to 7.0 / 8
)

private val COMMON_FRACTIONS_CLASS = COMMON_FRACTIONS.keys.joinToString("", "[", "]")

private fun parseNumber(value: String, numberDefs: Map<String, Double>): Double {
    val text = value.trim().replace(',', '.')
    if(text.isEmpty()) return 0.0

    numberDefs[text.lowercase().replace(WHITESPACE, " ")]?.let { return it }

    COMMON_FRACTIONS[text.last()]?.let { fraction ->
        return (text.dropLast(1).trim().toDoubleOrNull() ?: 0.0) + fraction
    }

    val slash = text.indexOf('/')
    if(slash == -1) return text.toDoubleOrNull() ?: 0.0

    val denominator = text.substring(slash + 1).toDoubleOrNull()?.takeIf { it != 0.0 } ?: return 0.0
    val head = text.substring(0, slash).trim()
    val numerator = head.substringAfterLast(' ').toDoubleOrNull() ?: 0.0
    val whole = head.substringBeforeLast(' ', "").trim().toDoubleOrNull() ?: 0.0

    return whole + numerator / denominator
}

private fun parseToSeconds(defs: TimerDetectionDefs, value: String, multiplier: Double): Int =
    (parseNumber(value, defs.numberDefs) * multiplier).roundToInt()

private val WHITESPACE = Regex("""\s+""")

/** Literal whitespace has to survive the pattern's COMMENTS mode. */
private fun Iterable<String>.toRegexAlt(): String =
    sortedByDescending(String::length)
        .joinToString("|") { definition ->
            definition.split(WHITESPACE).joinToString("""\s+""") { Regex.escape(it) }
        }

fun detectTimers(markdown: String, defs: TimerDetectionDefs): String {

    val hours = defs.hourDefs.toRegexAlt()
    val minutes = defs.minuteDefs.toRegexAlt()
    val seconds = defs.secondDefs.toRegexAlt()
    val andWords = defs.andDefs.toRegexAlt()
    val numberWords = defs.numberDefs.keys
        .takeIf(Set<String>::isNotEmpty)
        ?.let { "|${it.toRegexAlt()}" }
        .orEmpty()
    // Mixed fractions ("2 1/2", "2 1/2") come first so the whole figure wins over its parts.
    val number = """(?:
        [0-9]+\s+[0-9]+/[0-9]+
        |[0-9]+\s*$COMMON_FRACTIONS_CLASS
        |[0-9]+/[0-9]+
        |$COMMON_FRACTIONS_CLASS
        |[0-9]+(?:[.,][0-9]+)?
        $numberWords
    )"""
    val unit = "$hours|$minutes|$seconds"

    val qualifier = defs.rangeQualifierDefs
        .takeIf(Set<String>::isNotEmpty)
        ?.toRegexAlt()
        ?.let { """(?:(?:$it)\s+)?""" }
        .orEmpty()

    // Every dash like separator
    val dash =
        """[-\u058A\u05BE\u1400\u1806\u2010-\u2015\u2053\u2212\u2E17\u2E1A\u2E3A\u2E3B\u2E40\u2E5D\u301C\u3030\u30A0\uFE31\uFE32\uFE58\uFE63\uFF0D]"""

    val rangeSep = if (defs.rangeDefs.isNotEmpty()) {
        val rangeWords = defs.rangeDefs.toRegexAlt()
        """(?:\s*$dash\s*|\s+(?:$rangeWords)\s+$qualifier)"""
    } else {
        """\s*$dash\s*"""
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
        val next = markdown.getOrNull(m.range.last + 1)
        val unitEnd = m.value.last()
        if (next != null && (next.isLetter() || next.isDigit()) &&
            !next.isIdeographic() && !unitEnd.isIdeographic()
        ) return@replace m.value

        fun named(n: String) = m.groups[n]?.value.orEmpty()
        fun timer(s: Int) = "[**⏲ ${m.value}**](timer://$s)"
        fun range(from: Int, to: Int) = "[**⏲ ${m.value}**](timer-range://$from/$to)"

        when {
            named("crossFrom").isNotBlank() -> range(
                parseToSeconds(defs, named("crossFrom"), unitMultiplier(named("crossFromUnit"))),
                parseToSeconds(defs, named("crossTo"), unitMultiplier(named("crossToUnit")))
            )

            named("sameFrom").isNotBlank() -> {
                val mult = unitMultiplier(named("sameUnit"))
                range(
                    parseToSeconds(defs, named("sameFrom"), mult),
                    parseToSeconds(defs, named("sameTo"), mult)
                )
            }

            named("hoursOnly").isNotBlank() ->
                timer(parseToSeconds(defs, named("hoursOnly"), 3600.0))

            named("secondsOnly").isNotBlank() ->
                timer(parseToSeconds(defs, named("secondsOnly"), 1.0))

            else -> timer(
                parseToSeconds(defs, named("comboHours").ifBlank { "0" }, 3600.0) +
                        parseToSeconds(defs, named("comboMinutes").ifBlank { "0" }, 60.0)
            )
        }
    }
}
