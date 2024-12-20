package de.kitshn.ui.component.search.chips

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.StarRate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import de.kitshn.ui.component.icons.FiveStarIconRow
import de.kitshn.ui.component.input.StarRatingSelectionInput
import de.kitshn.ui.component.search.AdditionalSearchSettingsChipRowState
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_apply
import kitshn.composeapp.generated.resources.action_remove
import kitshn.composeapp.generated.resources.search_rating_filter
import org.jetbrains.compose.resources.stringResource

@Composable
fun MinimumRatingSearchSettingChip(
    state: AdditionalSearchSettingsChipRowState
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var currentRatingSelection by remember { mutableIntStateOf(state.minimumRating ?: 0) }

    FilterChip(
        selected = state.minimumRating != null,
        onClick = {
            showDialog = true
        },
        label = {
            FiveStarIconRow(rating = (state.minimumRating ?: 0).toDouble())
        }
    )

    if(showDialog) AlertDialog(
        onDismissRequest = {
            showDialog = false
        },
        icon = {
            Icon(Icons.Rounded.StarRate, stringResource(Res.string.search_rating_filter))
        },
        title = {
            Text(text = stringResource(Res.string.search_rating_filter))
        },
        text = {
            StarRatingSelectionInput(
                value = currentRatingSelection
            ) {
                currentRatingSelection = it
            }
        },
        dismissButton = {
            if(state.minimumRating != null) FilledTonalButton(onClick = {
                showDialog = false
                state.minimumRating = null

                state.update()
            }) {
                Text(stringResource(Res.string.action_remove))
            }
        },
        confirmButton = {
            Button(onClick = {
                showDialog = false
                state.minimumRating = currentRatingSelection

                state.update()
            }) {
                Text(stringResource(Res.string.action_apply))
            }
        }
    )
}