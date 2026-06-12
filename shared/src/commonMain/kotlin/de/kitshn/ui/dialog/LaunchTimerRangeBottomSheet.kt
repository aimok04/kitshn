package de.kitshn.ui.dialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.kitshn.time.formatTimerSeconds
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.action_continue
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun rememberLaunchTimerRangeBottomSheetState(): LaunchTimerRangeBottomSheetState {
    return remember {
        LaunchTimerRangeBottomSheetState()
    }
}

class LaunchTimerRangeBottomSheetState(
    val shown: MutableState<Boolean> = mutableStateOf(false),
    val from: MutableState<Int> = mutableStateOf(0),
    val to: MutableState<Int> = mutableStateOf(0)
) {
    var value = 0
    var callback: (seconds: Int) -> Unit = {}

    fun open(
        from: Int,
        to: Int,
        callback: (seconds: Int) -> Unit
    ) {
        this.shown.value = true
        this.from.value = from
        this.to.value = to
        this.value = from + (to - from) / 2
        this.callback = callback
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaunchTimerRangeBottomSheet(
    state: LaunchTimerRangeBottomSheetState
) {
    val coroutineScope = rememberCoroutineScope()

    if(state.shown.value) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { state.dismiss() }
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Range <=2min in 1s steps, <1h in 1min steps, >=1h in 15min steps
                val stepSize = when {
                    state.to.value <= 120 -> 1
                    state.to.value < 3600 -> 60
                    else -> 900
                }
                val fromStep = state.from.value / stepSize
                val toStep = state.to.value / stepSize
                val initStep = state.value / stepSize
                val sliderSteps = maxOf(0, toStep - fromStep - 1)

                val sliderState = rememberSliderState(
                    value = initStep.toFloat(),
                    steps = sliderSteps,
                    valueRange = fromStep.toFloat()..toStep.toFloat(),
                )

                AnimatedContent(
                    targetState = sliderState.value.roundToInt() * stepSize
                ) { selectedSeconds ->
                    Text(
                        text = "⏲ ${selectedSeconds.formatTimerSeconds()}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.displaySmall
                    )
                }

                Slider(state = sliderState)

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        coroutineScope.launch {
                            sheetState.hide()
                            state.dismiss()
                            state.callback(sliderState.value.roundToInt() * stepSize)
                        }
                    }
                ) {
                    Text(stringResource(Res.string.action_continue))
                }
            }
        }
    }
}
