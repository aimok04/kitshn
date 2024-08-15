package de.kitshn.android.ui.component.search.chips

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
import androidx.compose.ui.res.stringResource
import de.kitshn.android.R
import de.kitshn.android.ui.component.icons.FiveStarIconRow
import de.kitshn.android.ui.component.input.StarRatingSelectionInput
import de.kitshn.android.ui.component.search.AdditionalSearchSettingsChipRowState

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
            Icon(Icons.Rounded.StarRate, stringResource(R.string.search_rating_filter))
        },
        title = {
            Text(text = stringResource(R.string.search_rating_filter))
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
                Text(stringResource(R.string.action_remove))
            }
        },
        confirmButton = {
            Button(onClick = {
                showDialog = false
                state.minimumRating = currentRatingSelection

                state.update()
            }) {
                Text(stringResource(R.string.action_apply))
            }
        }
    )
}