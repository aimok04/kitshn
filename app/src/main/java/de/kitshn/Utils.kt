package de.kitshn

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.view.Window
import android.view.WindowManager
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogWindowProvider
import de.kitshn.api.tandoor.model.TandoorFood
import de.kitshn.api.tandoor.model.TandoorKeyword
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.internal.FormatLanguage
import kotlinx.serialization.json.jsonPrimitive
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.floor

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
        return NumberFormat.getNumberInstance().apply {
            maximumFractionDigits = 2
        }.format(this)
    }
}

fun Context.launchCustomTabs(url: String) {
    CustomTabsIntent.Builder().build()
        .launchUrl(this, Uri.parse(url))
}

tailrec fun Context.getActivityWindow(): Window? =
    when(this) {
        is Activity -> window
        is ContextWrapper -> baseContext.getActivityWindow()
        else -> null
    }

@Composable
fun getDialogWindow(): Window? = (LocalView.current.parent as? DialogWindowProvider)?.window

@Composable
fun getActivityWindow(): Window? = LocalView.current.context.getActivityWindow()

@Composable
fun KeepScreenOn() {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val window = context.getActivityWindow()
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
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

fun Boolean.toTFString(): String {
    return if(this) "true" else "false"
}

fun String.parseTandoorDate(): LocalDate {
    if(this.length > 14) {
        val timeFormatter = DateTimeFormatter.ISO_DATE_TIME
        return LocalDate.parse(this, timeFormatter)
    }

    // legacy for version < 1.15.18
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return LocalDate.parse(this, dateFormatter)
}

fun String.parseUtcTandoorDate(): LocalDate {
    return Instant.parse(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

fun String.parseIsoTime(): LocalDateTime {
    val timeFormatter = DateTimeFormatter.ISO_DATE_TIME
    return LocalDateTime.parse(this, timeFormatter)
}

fun LocalDate.toTandoorDate(): String {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return dateFormatter.format(this)
}

fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

@Composable
fun LocalDate.toHumanReadableDateLabel(): String {
    val today = LocalDate.now()
    val diff = this.toEpochDay() - LocalDate.now().toEpochDay()

    return when(diff) {
        in 3L..6L -> {
            val dateFormat = DateTimeFormatter.ofPattern("EEEE")
            this.format(dateFormat)
        }

        2L -> stringResource(R.string.common_day_after_tomorrow)
        1L -> stringResource(R.string.common_tomorrow)
        0L -> stringResource(R.string.common_today)
        -1L -> stringResource(R.string.common_yesterday)
        -2L -> stringResource(R.string.common_day_before_yesterday)
        else -> {
            if(this.year == today.year) {
                val dateFormat = DateTimeFormatter.ofPattern("EE, dd. MMM.")
                this.format(dateFormat)
            } else {
                val dateFormat = DateTimeFormatter.ofPattern("dd. MMMM yyyy")
                this.format(dateFormat)
            }
        }
    }
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

fun String?.redactForRelease(): String {
    return if(BuildConfig.DEBUG) {
        this.toString()
    } else {
        "*** REDACTED ***"
    }
}

fun Context.launchMarketPage(packageName: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
    } catch(e: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
        )
    }
}

fun String.extractUrl(delimiters: String = "") = this
    .split(delimiters)
    .firstOrNull { it.startsWith("http://") || it.startsWith("https://") }