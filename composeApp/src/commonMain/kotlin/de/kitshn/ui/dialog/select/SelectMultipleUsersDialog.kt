package de.kitshn.ui.dialog.select

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups2
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.route.TandoorUser
import de.kitshn.removeIf
import de.kitshn.scoreMatch
import de.kitshn.ui.component.alert.FullSizeAlertPane
import de.kitshn.ui.component.input.AlwaysDockedSearchBar
import de.kitshn.ui.layout.ResponsiveSideBySideLayout
import de.kitshn.ui.modifier.fullWidthAlertDialogPadding
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_apply
import kitshn.composeapp.generated.resources.search_users
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberSelectMultipleUsersDialogState(): SelectMultipleUsersDialogState {
    return remember {
        SelectMultipleUsersDialogState()
    }
}

class SelectMultipleUsersDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {
    val selectedUsers = mutableStateListOf<TandoorUser>()

    fun open(selectedUsers: List<TandoorUser>) {
        this.selectedUsers.clear()
        this.selectedUsers.addAll(selectedUsers)

        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@Composable
fun SelectMultipleUsersDialog(
    client: TandoorClient,
    title: String = stringResource(Res.string.search_users),
    emptyErrorText: String,
    state: SelectMultipleUsersDialogState,
    prepend: @Composable () -> Unit = {},
    onSubmit: (users: List<TandoorUser>) -> Unit
) {
    if(!state.shown.value) return

    AlertDialog(
        modifier = Modifier.fullWidthAlertDialogPadding(),
        onDismissRequest = {
            state.dismiss()
        },
        icon = {
            Icon(Icons.Rounded.Groups2, title)
        },
        title = {
            Text(title)
        },
        text = {
            Column {
                prepend()

                BoxWithConstraints {
                    ResponsiveSideBySideLayout(
                        showDivider = true,

                        leftMinWidth = 200.dp,
                        rightMinWidth = 200.dp,

                        maxHeight = 800.dp,

                        leftLayout = { enoughSpace ->
                            Box(
                                Modifier.height(
                                    if(enoughSpace)
                                        this@BoxWithConstraints.maxHeight
                                    else
                                        (this@BoxWithConstraints.maxHeight - 32.dp) / 2f
                                ),
                            ) {
                                UserSearchBar(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(),
                                    client = client,
                                    selectedUsers = state.selectedUsers
                                ) { user, value ->
                                    if(value) {
                                        state.selectedUsers.add(0, user)
                                    } else {
                                        state.selectedUsers.removeIf { it.id == user.id }
                                    }
                                }
                            }
                        }
                    ) {
                        Box(
                            Modifier.fillMaxHeight()
                        ) {
                            if(state.selectedUsers.size == 0) {
                                FullSizeAlertPane(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = emptyErrorText,
                                    text = emptyErrorText
                                )
                            } else {
                                LazyColumn(
                                    Modifier.clip(RoundedCornerShape(16.dp))
                                ) {
                                    items(
                                        state.selectedUsers.size,
                                        key = { state.selectedUsers[it].id }) {
                                        val user = state.selectedUsers[it]

                                        UserCheckedListItem(
                                            checked = true,
                                            user = user
                                        ) {
                                            state.selectedUsers.removeIf { it.id == user.id }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                state.dismiss()
                onSubmit(state.selectedUsers)
            }) {
                Text(stringResource(Res.string.action_apply))
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    )
}

@Composable
fun UserCheckedListItem(
    modifier: Modifier = Modifier,
    checked: Boolean,
    user: TandoorUser,
    onCheckedChange: (value: Boolean) -> Unit
) {
    ListItem(
        modifier = modifier
            .clickable {
                onCheckedChange(!checked)
            },
        leadingContent = {
            Checkbox(
                checked = checked,
                onCheckedChange = {
                    onCheckedChange(it)
                }
            )
        },
        headlineContent = {
            Text(user.display_name)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSearchBar(
    modifier: Modifier = Modifier,
    client: TandoorClient,
    selectedUsers: List<TandoorUser>,
    onCheckedChange: (user: TandoorUser, value: Boolean) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var query by rememberSaveable { mutableStateOf("") }
    var search by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(query) {
        delay(250)
        search = query
    }

    val availableUsers = remember { mutableStateListOf<TandoorUser>() }
    LaunchedEffect(Unit) { availableUsers.addAll(client.user.getUsers()) }

    val searchResults = remember { mutableStateListOf<TandoorUser>() }
    LaunchedEffect(availableUsers.toList(), search) {
        searchResults.clear()
        searchResults.addAll(
            if(search.isBlank()) {
                availableUsers.sortedBy { it.display_name }
            } else {
                availableUsers.sortedByDescending { it.display_name.scoreMatch(search) }
            }
        )
    }

    AlwaysDockedSearchBar(
        modifier = modifier,
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            dividerColor = Color.Transparent
        ),
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = { query = it },
                onSearch = {
                    keyboardController?.hide()
                    search = it
                },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Search,
                        stringResource(Res.string.search_users)
                    )
                },
                placeholder = { Text(stringResource(Res.string.search_users)) },
                expanded = true,
                onExpandedChange = { }
            )
        }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(searchResults.size) {
                val user = searchResults[it]

                UserCheckedListItem(
                    checked = selectedUsers.find { it.id == user.id } != null,
                    user = user
                ) { checked ->
                    keyboardController?.hide()
                    onCheckedChange(user, checked)
                }
            }
        }
    }
}