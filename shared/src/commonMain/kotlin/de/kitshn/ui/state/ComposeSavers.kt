package de.kitshn.ui.state

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

fun <T : Any> mutableStateSaver(innerElementSaver: Saver<T, out Any>) = Saver<MutableState<T>, Any>(
    save = { state ->
        with(innerElementSaver as Saver<T, Any>) { save(state.value) } ?: "null"
    },
    restore = { value ->
        if (value == "null") throw IllegalArgumentException("Cannot restore null value")
        else mutableStateOf((innerElementSaver as Saver<T, Any>).restore(value)!!)
    }
)

val ColorSaver = Saver<Color, Long>(
    save = { it.value.toLong() },
    restore = { Color(it.toULong()) }
)

val ColorStateSaver = mutableStateSaver(ColorSaver)

val LocalTimeSaver = Saver<LocalTime, String>(
    save = { it.toString() },
    restore = { LocalTime.parse(it) }
)

val LocalTimeStateSaver = mutableStateSaver(LocalTimeSaver)

val LocalDateSaver = Saver<LocalDate, String>(
    save = { it.toString() },
    restore = { LocalDate.parse(it) }
)

val LocalDateStateSaver = mutableStateSaver(LocalDateSaver)
