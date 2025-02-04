package de.kitshn.ui.component.shopping.chips

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import de.kitshn.ui.component.shopping.AdditionalShoppingSettingsChipRowState
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_category
import kitshn.composeapp.generated.resources.common_creator
import kitshn.composeapp.generated.resources.common_group_by
import kitshn.composeapp.generated.resources.common_none
import kitshn.composeapp.generated.resources.common_recipe
import kitshn.composeapp.generated.resources.common_select
import kitshn.composeapp.generated.resources.common_selected
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class GroupingOptions(
    val label: StringResource
) {
    NONE(Res.string.common_none),
    BY_CATEGORY(Res.string.common_category),
    BY_RECIPE(Res.string.common_recipe),
    BY_CREATOR(Res.string.common_creator);

    @Composable
    fun itemLabel(): String {
        return stringResource(this.label)
    }
}

@Composable
fun GroupingSettingChip(
    state: AdditionalShoppingSettingsChipRowState
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    FilterChip(
        selected = state.grouping != GroupingOptions.NONE,
        onClick = {
            showDialog = true
        },
        label = {
            Text(text = buildAnnotatedString {
                append(stringResource(Res.string.common_group_by))

                if(state.grouping != GroupingOptions.NONE) {
                    append(": ")
                    append(state.grouping.itemLabel())
                }
            })
        },
        trailingIcon = {
            Icon(Icons.Rounded.ArrowDropDown, stringResource(Res.string.common_select))
        }
    )

    if(showDialog) AlertDialog(
        onDismissRequest = {
            showDialog = false
        },
        icon = {
            Icon(Icons.Rounded.Groups, stringResource(Res.string.common_group_by))
        },
        title = {
            Text(text = stringResource(Res.string.common_group_by))
        },
        text = {
            Column(
                Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .verticalScroll(rememberScrollState())
            ) {
                GroupingOptions.entries.forEach {
                    ListItem(
                        modifier = Modifier.clickable {
                            showDialog = false
                            state.grouping = it

                            state.update()
                        },
                        headlineContent = {
                            Text(text = it.itemLabel())
                        },
                        trailingContent = {
                            if(state.grouping != it) return@ListItem
                            Icon(Icons.Rounded.Check, stringResource(Res.string.common_selected))
                        }
                    )
                }
            }
        },
        confirmButton = { }
    )
}