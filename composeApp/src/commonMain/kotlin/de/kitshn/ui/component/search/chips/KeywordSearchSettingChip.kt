package de.kitshn.ui.component.search.chips

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.ui.component.search.AdditionalSearchSettingsChipRowState
import de.kitshn.ui.component.settings.SettingsSwitchListItem
import de.kitshn.ui.dialog.select.SelectMultipleKeywordsDialog
import de.kitshn.ui.dialog.select.rememberSelectMultipleKeywordsDialogState
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_tags
import kitshn.composeapp.generated.resources.search_keyword_including_all_description
import kitshn.composeapp.generated.resources.search_keyword_including_all_label
import org.jetbrains.compose.resources.stringResource

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
                label = {
                    Text(text = stringResource(Res.string.search_keyword_including_all_label))
                },
                description = {
                    Text(text = stringResource(Res.string.search_keyword_including_all_description))
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
            Text(stringResource(Res.string.common_tags) + if(selected) " (${state.selectedKeywords.size})" else "")
        }
    )
}