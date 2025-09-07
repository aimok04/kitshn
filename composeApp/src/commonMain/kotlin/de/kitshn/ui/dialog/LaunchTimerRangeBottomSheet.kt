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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.kitshn.formatDuration
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_continue
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
    var value by mutableStateOf(0)

    var callback: (value: Int) -> Unit = {}

    fun open(
        from: Int,
        to: Int,
        callback: (value: Int) -> Unit
    ) {
        this.shown.value = true
        this.from.value = (from / 60)
        this.to.value = (to / 60)

        this.value = this.from.value + ((this.to.value - this.from.value) / 2)
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
            onDismissRequest = {
                state.dismiss()
            }
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val sliderState = rememberSliderState(
                    value = state.value.toFloat(),
                    steps = (state.to.value - state.from.value) - 1,
                    valueRange = state.from.value.toFloat()..state.to.value.toFloat(),
                )

                AnimatedContent(
                    targetState = sliderState.value.roundToInt()
                ) {
                    Text(
                        text = "⏲ ${it.formatDuration()}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.displaySmall
                    )
                }

                Slider(
                    state = sliderState
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        coroutineScope.launch {
                            sheetState.hide()
                            state.dismiss()

                            state.callback(sliderState.value.roundToInt() * 60)
                        }
                    }
                ) {
                    Text(stringResource(Res.string.action_continue))
                }
            }
        }
    }
}