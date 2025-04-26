package de.kitshn

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalHapticFeedback
import co.touchlab.kermit.Logger
import de.kitshn.api.tandoor.model.TandoorFood
import de.kitshn.api.tandoor.model.TandoorKeyword
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_day_after_tomorrow
import kitshn.composeapp.generated.resources.common_day_before_yesterday
import kitshn.composeapp.generated.resources.common_hour_short
import kitshn.composeapp.generated.resources.common_minute_min
import kitshn.composeapp.generated.resources.common_today
import kitshn.composeapp.generated.resources.common_tomorrow
import kitshn.composeapp.generated.resources.common_yesterday
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.internal.FormatLanguage
import kotlinx.serialization.json.jsonPrimitive
import nl.jacobras.humanreadable.HumanReadable
import org.jetbrains.compose.resources.stringResource
import kotlin.math.floor

enum class FileFormats(val extensions: List<String>, val mimeType: String) {
    EPUB(listOf("epub"), "application/epub+zip"),
    JSON(listOf("json"), "application/json"),
    JSON_LD(listOf("jsonld"), "application/ld+json"),
    MS_WORD(listOf("doc"), "application/msword"),
    MS_WORD_OPEN_XML(
        listOf("docx"),
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    ),
    PDF(listOf("pdf"), "application/pdf"),
    RTF(listOf("rtf"), "application/rtf"),
    XML(listOf("xml"), "application/xml"),
    BMP(listOf("bmp"), "image/bmp"),
    GIF(listOf("gif"), "image/gif"),
    JPEG(listOf("jpg", "jpeg"), "image/jpeg"),
    PNG(listOf("png"), "image/png"),
    SVG_XML(listOf("svg"), "image/svg+xml"),
    TIFF(listOf("tif", "tiff"), "image/tiff"),
    WEBP(listOf("webp"), "image/webp"),
    HEIC(listOf("heic"), "image/heic"),
    HEIF(listOf("heif"), "image/heif"),
    AVIF(listOf("avif", "avifs"), "image/avif");

    companion object {
        fun findMimeType(extension: String): String? {
            return entries.find { it.extensions.contains(extension) }?.mimeType
        }
    }
}

enum class HapticFeedbackType {
    LONG_PRESS,
    SHORT_TICK,
    DRAG_START,
    GESTURE_START,
    GESTURE_END
}

@Composable
expect fun osDependentHapticFeedbackHandler(): ((type: HapticFeedbackType) -> Unit)?

@Composable
fun HapticFeedbackHandler(): (type: HapticFeedbackType) -> Unit {
    val hapticFeedback = LocalHapticFeedback.current

    val handler = osDependentHapticFeedbackHandler() ?: {
        hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
    }

    return handler
}

//source code from androidX
fun String.toColorInt(): Int {
    if(this[0] == '#') {
        var color = substring(1).toLong(16)
        if(length == 7) {
            color = color or 0x00000000ff000000L
        } else if(length != 9) {
            throw IllegalArgumentException("Unknown color")
        }
        return color.toInt()
    }
    throw IllegalArgumentException("Unknown color")
}

val json = Json { ignoreUnknownKeys = true }

object JsonAsStringSerializer :
    JsonTransformingSerializer<String>(tSerializer = String.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return JsonPrimitive(value = element.jsonPrimitive.content)
    }
}

@OptIn(InternalSerializationApi::class)
inline fun <reified T> Json.maybeDecodeFromString(
    @FormatLanguage(
        "json",
        "",
        ""
    ) string: String
): T? {
    return try {
        decodeFromString<T>(string)
    } catch(e: Exception) {
        null
    }
}

fun formatDecimalToFraction(decimal: Double): String {
    if(decimal == 0.0) return ""

    if(decimal <= 0.2) {
        return "⅕"
    } else if(decimal <= 0.27) {
        return "¼"
    } else if(decimal <= 0.35) {
        return "⅓"
    } else if(decimal <= 0.55) {
        return "½"
    } else if(decimal <= 0.68) {
        return "⅔"
    } else if(decimal <= 0.78) {
        return "¾"
    } else if(decimal > 0.8) {
        return "⅘"
    }

    return ""
}

fun Double.formatAmount(fractional: Boolean = true): String {
    if(fractional) {
        val int = floor(this).toInt()
        val decimal = this - int

        val value = if(int == 0) "" else "$int "
        return "$value${formatDecimalToFraction(decimal)}"
    } else {
        try {
            return HumanReadable.number(this, 2).run {
                if(endsWith("00")) {
                    substring(0, length - 3)
                } else if(endsWith("0")) {
                    substring(0, length - 1)
                } else {
                    this
                }
            }
        } catch(e: Exception) {
            Logger.e("Utils.kt", e)
            return this.toString()
        }
    }
}

fun Int.withLeadingZeros(length: Int): String {
    val output = StringBuilder()
    repeat(length - toString().length) { output.append("0") }
    output.append(toString())
    return output.toString()
}

@Composable
fun ScrollState.isScrollingUp(): Boolean {
    var previousValue by remember(this) {
        mutableIntStateOf(value)
    }

    return remember(this) {
        derivedStateOf {
            if(previousValue != value) {
                previousValue > value
            } else {
                value < 100
            }.also {
                previousValue = value
            }
        }
    }.value
}

@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) {
        mutableIntStateOf(firstVisibleItemIndex)
    }
    var previousScrollOffset by remember(this) {
        mutableIntStateOf(firstVisibleItemScrollOffset)
    }

    return remember(this) {
        derivedStateOf {
            if(previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

@Composable
fun LazyStaggeredGridState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) {
        mutableIntStateOf(firstVisibleItemIndex)
    }
    var previousScrollOffset by remember(this) {
        mutableIntStateOf(firstVisibleItemScrollOffset)
    }

    return remember(this) {
        derivedStateOf {
            if(previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

@Composable
fun LazyGridState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) {
        mutableIntStateOf(firstVisibleItemIndex)
    }
    var previousScrollOffset by remember(this) {
        mutableIntStateOf(firstVisibleItemScrollOffset)
    }

    return remember(this) {
        derivedStateOf {
            if(previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

fun Boolean.toTFString(): String {
    return if(this) "true" else "false"
}

@OptIn(FormatStringsInDatetimeFormats::class)
fun String.parseTandoorDate(): LocalDate {
    if(this.length > 14) {
        return Instant.parse(this).toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    // legacy for version < 1.15.18
    return LocalDate.parse(this, LocalDate.Format { byUnicodePattern("yyyy-MM-dd") })
}

fun String.parseUtcTandoorDate(): LocalDate {
    return Instant.parse(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
}

fun String.parseIsoTime(): LocalDateTime {
    return Instant.parse(this).toLocalDateTime(TimeZone.currentSystemDefault())
}

@OptIn(FormatStringsInDatetimeFormats::class)
fun LocalDate.toTandoorDate(): String {
    val dateFormat = LocalDate.Format { byUnicodePattern("yyyy-MM-dd") }
    return dateFormat.format(this)
}

fun Long.toLocalDate(): LocalDate {
    return Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
}

@Composable
expect fun LocalDate.format(pattern: String): String

@Composable
fun LocalDate.toHumanReadableDateLabel(): String {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val diff = this.toEpochDays() - today.toEpochDays()

    return when(diff) {
        in 3..6 -> {
            this.format("EEEE")
        }

        2 -> stringResource(Res.string.common_day_after_tomorrow)
        1 -> stringResource(Res.string.common_tomorrow)
        0 -> stringResource(Res.string.common_today)
        -1 -> stringResource(Res.string.common_yesterday)
        -2 -> stringResource(Res.string.common_day_before_yesterday)
        else -> {
            if(this.year == today.year) {
                this.format("EE, dd. MMM")
            } else {
                this.format("dd. MMMM yyyy")
            }
        }
    }
}

@Composable
fun Int.formatDuration(): String {
    val components = mutableListOf<String>()
    if(this > 59) components.add("${this / 60} ${stringResource(Res.string.common_hour_short)}")
    (this % 60).takeIf { it > 0 }?.let {
        components.add("$it ${stringResource(Res.string.common_minute_min)}")
    }
    return components.joinToString(separator = " ")
}

fun List<TandoorKeyword>.keywordToIdList(): List<Int> {
    val idList = mutableListOf<Int>()
    this.forEach { idList.add(it.id) }
    return idList
}

fun List<TandoorFood>.foodToIdList(): List<Int> {
    val idList = mutableListOf<Int>()
    this.forEach { idList.add(it.id) }
    return idList
}

internal fun LazyListState.reachedBottom(buffer: Int = 1): Boolean {
    val lastVisibleItem = this.layoutInfo.visibleItemsInfo.lastOrNull()
    return lastVisibleItem?.index != 0 && lastVisibleItem?.index == this.layoutInfo.totalItemsCount - buffer
}

internal fun LazyGridState.reachedBottom(buffer: Int = 1): Boolean {
    val lastVisibleItem = this.layoutInfo.visibleItemsInfo.lastOrNull()
    return lastVisibleItem?.index != 0 && lastVisibleItem?.index == this.layoutInfo.totalItemsCount - buffer
}

internal fun LazyStaggeredGridState.reachedBottom(buffer: Int = 1): Boolean {
    val lastVisibleItem = this.layoutInfo.visibleItemsInfo.lastOrNull()
    return lastVisibleItem?.index != 0 && lastVisibleItem?.index == this.layoutInfo.totalItemsCount - buffer
}

fun String.scoreMatch(other: String) =
    zip(other).count { it.first == it.second } / maxOf(length, other.length).toFloat()


// compare method for forms
fun String?.formEquals(input: String?): Boolean {
    if(this.isNullOrBlank() && input.isNullOrBlank()) return true
    if(this == input) return true
    return false
}

fun List<TandoorKeyword>?.formEquals(input: List<TandoorKeyword>?): Boolean {
    if(this == input) return true
    if(this?.size != input?.size) return false
    if(this == null || input == null) return false

    // cycle through first array and compare id with second
    repeat(this.size) {
        if(this[it].id != input[it].id)
            return false
    }

    return true
}

fun List<Int>?.formEqualsInt(input: List<Int>?): Boolean {
    if(this == input) return true
    if(this?.size != input?.size) return false
    if(this == null || input == null) return false

    // cycle through first array and compare with second
    repeat(this.size) {
        if(this[it] != input[it])
            return false
    }

    return true
}

// adds missing functionality
fun <T : Any> MutableList<T>.removeIf(check: (T) -> Boolean) {
    this.toImmutableList().forEach {
        if(check(it)) this.remove(it)
    }
}

fun String?.redactForRelease(): String {
    return if(platformDetails.debug) {
        this.toString()
    } else {
        "*** REDACTED ***"
    }
}

expect fun saveBreadcrumb(key: String, value: String)

@Composable
expect fun BackHandler(enabled: Boolean = true, handler: () -> Unit)

@Composable
expect fun KeepScreenOn()

@Composable
expect fun launchMarketPageHandler(): () -> Unit

@Composable
expect fun launchWebsiteHandler(): (url: String) -> Unit

@Composable
expect fun launchTimerHandler(): (seconds: Int, name: String) -> Unit

expect val isLaunchTimerHandlerImplemented: Boolean

@Composable
expect fun shareContentHandler(): (title: String, url: String) -> Unit

@Composable
expect fun closeAppHandler(): () -> Unit

fun String.extractUrl(delimiters: String = " ") = this
    .split(delimiters)
    .firstOrNull { it.startsWith("http://") || it.startsWith("https://") }