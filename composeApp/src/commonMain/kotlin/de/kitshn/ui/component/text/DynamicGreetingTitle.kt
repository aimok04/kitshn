package de.kitshn.ui.component.text

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.greeting_christmas
import kitshn.composeapp.generated.resources.greeting_good_evening
import kitshn.composeapp.generated.resources.greeting_good_morning
import kitshn.composeapp.generated.resources.greeting_good_night
import kitshn.composeapp.generated.resources.greeting_noon
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

data class DynamicGreetingCheckData(
    val dateTime: LocalDateTime
)

enum class DynamicGreetings(
    val content: StringResource,
    val check: (d: DynamicGreetingCheckData) -> Boolean
) {
    CHRISTMAS(Res.string.greeting_christmas, {
        it.dateTime.month == kotlinx.datetime.Month.DECEMBER && it.dateTime.dayOfMonth in 21..26
    }),
    MORNING(Res.string.greeting_good_morning, {
        it.dateTime.hour in 5..10
    }),
    MIDDAY(Res.string.greeting_noon, {
        it.dateTime.hour in 11..17
    }),
    EVENING(Res.string.greeting_good_evening, {
        it.dateTime.hour in 18..21
    }),
    NIGHT(Res.string.greeting_good_night, {
        (it.dateTime.hour in 22..24) || (it.dateTime.hour in 0..4)
    })
}

@Composable
fun DynamicGreetingTitle() {
    var greetingContent by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val data = DynamicGreetingCheckData(
            dateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )

        for(entry in DynamicGreetings.entries) {
            if(!entry.check(data)) continue
            greetingContent = getString(entry.content)
            break
        }
    }

    Text(text = greetingContent)
}