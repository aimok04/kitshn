package de.kitshn.ui.component.search.chips

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.kitshn.R
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.ui.component.search.AdditionalSearchSettingsChipRowState
import de.kitshn.ui.component.settings.SettingsSwitchListItem
import de.kitshn.ui.dialog.select.SelectMultipleKeywordsDialog
import de.kitshn.ui.dialog.select.rememberSelectMultipleKeywordsDialogState

@Composable
fun KeywordSearchSettingChip(
    client: TandoorClient,
    state: AdditionalSearchSettingsChipRowState
) {
    val selected = state.selectedKeywords.size > 0

    val dialogState = rememberSelectMultipleKeywordsDialogState()
    SelectMultipleKeywordsDialog(
        client = client,
        prepend = {
            SettingsSwitchListItem(
                contentPadding = PaddingValues(bottom = 8.dp),
                label = {
                    Text(text = stringResource(R.string.search_keyword_including_all_label))
                },
                description = {
                    Text(text = stringResource(R.string.search_keyword_including_all_description))
                },
                icon = Icons.Rounded.AllInclusive,
                checked = state.keywordsAnd,
                contentDescription = ""
            ) {
                state.keywordsAnd = it
            }
        },
        hideKeywordCreation = true,
        state = dialogState
    ) {
        state.selectedKeywords.clear()
        state.selectedKeywords.addAll(it)

        state.update()
    }

    FilterChip(
        selected = selected,
        onClick = {
            dialogState.open(
                state.selectedKeywords
            )
        },
        label = {
            Text(stringResource(R.string.common_tags) + if(selected) " (${state.selectedKeywords.size})" else "")
        }
    )
}