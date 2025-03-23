package de.kitshn.ui.component.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.route.TandoorUser
import de.kitshn.ui.dialog.select.SelectMultipleUsersDialog
import de.kitshn.ui.dialog.select.rememberSelectMultipleUsersDialogState
import de.kitshn.ui.theme.Typography
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_add
import kitshn.composeapp.generated.resources.action_remove
import org.jetbrains.compose.resources.stringResource

@Composable
fun SelectUsersField(
    modifier: Modifier = Modifier,
    client: TandoorClient,
    value: List<TandoorUser>,
    label: @Composable (() -> Unit)? = null,
    dialogTitle: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    onValueChange: (List<TandoorUser>) -> Unit
) {
    val selectMultipleUsersDialogState = rememberSelectMultipleUsersDialogState()

    Column(
        modifier = modifier.padding(top = 4.dp, bottom = 4.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = TextFieldDefaults.colors().focusedContainerColor,
            contentColor = TextFieldDefaults.colors().focusedLeadingIconColor,
            shape = TextFieldDefaults.shape
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if(leadingIcon != null) {
                    Box(
                        Modifier.padding(16.dp)
                    ) {
                        leadingIcon()
                    }
                }

                Column {
                    Spacer(Modifier.height(8.dp))

                    CompositionLocalProvider(
                        LocalTextStyle provides Typography().labelMedium,
                    ) {
                        if(label != null) {
                            label()
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            InputChip(
                                selected = false,
                                onClick = {
                                    selectMultipleUsersDialogState.open(mutableListOf<TandoorUser>().apply {
                                        addAll(
                                            value
                                        )
                                    })
                                },
                                label = {
                                    Icon(Icons.Rounded.Add, stringResource(Res.string.action_add))
                                }
                            )
                        }

                        items(value.size, key = { value[it].id }) {
                            val user = value[it]

                            InputChip(
                                selected = true,
                                label = { Text(user.display_name) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Rounded.Close,
                                        stringResource(Res.string.action_remove)
                                    )
                                },
                                onClick = {
                                    onValueChange(
                                        value.filterNot {
                                            it.id == user.id
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        Box(
            Modifier.height(1.dp).fillMaxWidth()
                .background(MaterialTheme.colorScheme.onSurfaceVariant)
        )
    }

    SelectMultipleUsersDialog(
        client = client,
        title = dialogTitle,
        state = selectMultipleUsersDialogState,
        onSubmit = {
            Logger.d("SelectUserFields (submit)") { it.toString() }
            onValueChange(it)
        }
    )
}