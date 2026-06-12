package de.kitshn.time

import androidx.compose.runtime.Composable
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.common_hour_short
import kitshn.shared.generated.resources.common_minute_min
import org.jetbrains.compose.resources.stringResource

@Composable
fun Int.formatTimerSeconds(): String {
    val h = this / 3600
    val m = (this % 3600) / 60
    val s = this % 60
    val hourStr = stringResource(Res.string.common_hour_short)
    val minStr = stringResource(Res.string.common_minute_min)
    return buildList {
        if (h > 0) add("$h $hourStr")
        if (m > 0) add("$m $minStr")
        if (s > 0 || (h == 0 && m == 0)) add("${s}s")
    }.joinToString(" ")
}
