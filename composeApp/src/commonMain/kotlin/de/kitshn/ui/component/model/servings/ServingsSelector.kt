package de.kitshn.ui.component.model.servings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import de.kitshn.formatAmount
import de.kitshn.ui.dialog.servings.ServingsChangeDialog
import de.kitshn.ui.dialog.servings.rememberServingsChangeDialogState
import de.kitshn.ui.modifier.loadingPlaceHolder
import de.kitshn.ui.state.ErrorLoadingSuccessState
import de.kitshn.ui.theme.Typography
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_add
import kitshn.composeapp.generated.resources.action_minus
import kitshn.composeapp.generated.resources.common_plural_portion
import kitshn.composeapp.generated.resources.common_portions
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ServingsSelector(
    value: Double,
    label: String,
    loadingState: ErrorLoadingSuccessState = ErrorLoadingSuccessState.SUCCESS,
    onChange: (value: Double) -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current

    fun valueChange(value: Double) {
        if(value < 0) return

        onChange(
            value
        )
    }

    val servingsChangeDialogState = rememberServingsChangeDialogState()
    ServingsChangeDialog(
        portionText = label.ifBlank {
            stringResource(Res.string.common_portions)
        },
        state = servingsChangeDialogState
    ) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
        onChange(it)
    }

    Row(
        Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SmallFloatingActionButton(
            modifier = Modifier.loadingPlaceHolder(loadingState),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp
            ),
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                valueChange(value - 1)
            }
        ) {
            Icon(Icons.Rounded.Remove, stringResource(Res.string.action_minus))
        }

        Spacer(Modifier.width(4.dp))

        AssistChip(
            modifier = Modifier.loadingPlaceHolder(loadingState),
            onClick = {
                servingsChangeDialogState.open(value)
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            border = null,
            shape = FloatingActionButtonDefaults.smallShape,
            label = {
                Text(
                    modifier = Modifier.padding(12.dp),
                    style = Typography().labelMedium,
                    text = if(label.isNotBlank()) {
                        "${value.formatAmount(fractional = false)} $label"
                    } else {
                        pluralStringResource(
                            Res.plurals.common_plural_portion,
                            if(value.toString().endsWith(".0")) {
                                value.toInt()
                            } else {
                                Int.MAX_VALUE
                            },
                            value.formatAmount(
                                fractional = false
                            )
                        )
                    }
                )
            }
        )

        Spacer(Modifier.width(4.dp))

        SmallFloatingActionButton(
            modifier = Modifier.loadingPlaceHolder(loadingState),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp
            ),
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                valueChange(value + 1)
            }
        ) {
            Icon(Icons.Rounded.Add, stringResource(Res.string.action_add))
        }
    }
}