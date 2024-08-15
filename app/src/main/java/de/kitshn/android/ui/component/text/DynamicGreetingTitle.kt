package de.kitshn.android.ui.component.text

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import de.kitshn.android.R
import java.time.LocalDateTime

data class DynamicGreetingCheckData(
    val dateTime: LocalDateTime
)

enum class DynamicGreetings(
    val content: Int,
    val check: (d: DynamicGreetingCheckData) -> Boolean
) {
    CHRISTMAS(R.string.greeting_christmas, {
        it.dateTime.monthValue == 12 && it.dateTime.dayOfMonth in 21..26
    }),
    MORNING(R.string.greeting_good_morning, {
        it.dateTime.hour in 5..10
    }),
    MIDDAY(R.string.greeting_noon, {
        it.dateTime.hour in 11..17
    }),
    EVENING(R.string.greeting_good_evening, {
        it.dateTime.hour in 18..21
    }),
    NIGHT(R.string.greeting_good_night, {
        (it.dateTime.hour in 22..24) || (it.dateTime.hour in 0..4)
    })
}

@Composable
fun DynamicGreetingTitle() {
    val context = LocalContext.current

    var greetingContent by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val data = DynamicGreetingCheckData(
            dateTime = LocalDateTime.now()
        )

        for(entry in DynamicGreetings.entries) {
            if(!entry.check(data)) continue
            greetingContent = context.getString(entry.content)
            break
        }
    }

    Text(text = greetingContent)
}