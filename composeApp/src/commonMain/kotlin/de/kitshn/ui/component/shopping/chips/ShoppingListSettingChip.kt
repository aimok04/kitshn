package de.kitshn.ui.component.shopping.chips

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingList
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.model.route.ShoppingViewModel
import de.kitshn.removeIf
import de.kitshn.ui.component.model.shopping.ShoppingListColorPill
import de.kitshn.ui.component.shopping.AdditionalShoppingSettingsChipRowState
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_remove
import kitshn.composeapp.generated.resources.common_select
import kitshn.composeapp.generated.resources.common_selected
import kitshn.composeapp.generated.resources.common_shopping_list
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShoppingListSettingChip(
    vm: ShoppingViewModel,
    state: AdditionalShoppingSettingsChipRowState
) {
    val hapticFeedback = LocalHapticFeedback.current

    var showDialog by rememberSaveable { mutableStateOf(false) }

    FilterChip(
        selected = state.shoppingLists.isNotEmpty(),
        onClick = {
            showDialog = true
        },
        label = {
            Text(text = buildAnnotatedString {
                append(stringResource(Res.string.common_shopping_list))

                if(state.shoppingLists.isNotEmpty()) {
                    append(": ")

                    state.shoppingLists.forEachIndexed { index, item ->
                        append(item.name)
                        if(index != state.shoppingLists.lastIndex) append(", ")
                    }
                }
            })
        },
        trailingIcon = {
            Icon(Icons.Rounded.ArrowDropDown, stringResource(Res.string.common_select))
        }
    )

    if(showDialog) {
        val requestState = rememberTandoorRequestState()

        val shoppingLists = remember { mutableStateListOf<TandoorShoppingList>() }
        LaunchedEffect(vm.entries.toList()) {
            val ids = mutableSetOf<Long>()

            shoppingLists.clear()

            for(entry in vm.entries) {
                for(list in entry.shopping_lists) {
                    if(ids.contains(list.id))
                        continue

                    shoppingLists.add(list)
                    ids.add(list.id)
                }
            }
        }

        val selectedShoppingListIds = remember { mutableStateSetOf<Long>() }
        LaunchedEffect(state.shoppingLists.toList()) {
            selectedShoppingListIds.clear()
            selectedShoppingListIds.addAll(
                state.shoppingLists.map { it.id }
            )
        }

        AlertDialog(
            modifier = Modifier.padding(top = 24.dp, bottom = 24.dp),
            onDismissRequest = {
                showDialog = false
            },
            icon = {
                Icon(
                    Icons.AutoMirrored.Rounded.ListAlt,
                    stringResource(Res.string.common_shopping_list)
                )
            },
            title = {
                Text(text = stringResource(Res.string.common_shopping_list))
            },
            text = {
                LazyColumn(
                    Modifier
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    items(shoppingLists.size) {
                        val shoppingList = shoppingLists[it]

                        ListItem(
                            modifier = Modifier.clickable {
                                if(selectedShoppingListIds.contains(shoppingList.id)) {
                                    state.shoppingLists.removeIf { it.id == shoppingList.id }
                                } else {
                                    state.shoppingLists.add(shoppingList)
                                }

                                state.settings.setShoppingLists(state.shoppingLists.toList())
                                state.update()

                                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                            },
                            headlineContent = {
                                Text(text = shoppingList.name)
                            },
                            trailingContent = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    if(selectedShoppingListIds.contains(shoppingList.id)) {
                                        Icon(
                                            Icons.Rounded.Check,
                                            stringResource(Res.string.common_selected)
                                        )
                                    }

                                    Spacer(Modifier.width(4.dp))

                                    ShoppingListColorPill(
                                        shoppingList = shoppingList
                                    )
                                }
                            }
                        )
                    }
                }
            },
            dismissButton = {
                if(state.shoppingLists.isNotEmpty()) FilledTonalButton(onClick = {
                    showDialog = false
                    state.shoppingLists.clear()
                    state.settings.setShoppingLists(listOf())

                    state.update()
                }) {
                    Text(text = stringResource(Res.string.action_remove))
                }
            },
            confirmButton = { }
        )
    }
}