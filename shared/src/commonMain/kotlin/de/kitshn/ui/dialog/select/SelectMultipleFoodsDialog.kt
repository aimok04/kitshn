package de.kitshn.ui.dialog.select

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RestaurantMenu
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
import de.kitshn.api.tandoor.model.TandoorFood
import de.kitshn.repo.FoodRepo
import de.kitshn.ui.component.alert.FullSizeAlertPane
import de.kitshn.ui.component.input.AlwaysDockedSearchBar
import de.kitshn.ui.component.input.iosKeyboardWorkaround.InputFieldWithIOSKeyboardWorkaround
import de.kitshn.ui.layout.ResponsiveSideBySideLayout
import de.kitshn.ui.modifier.fullWidthAlertDialogPadding
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.action_apply
import kitshn.shared.generated.resources.search_ingredients
import kitshn.shared.generated.resources.search_ingredients_filter
import kitshn.shared.generated.resources.search_ingredients_filter_empty
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun rememberSelectMultipleFoodsDialogState(): SelectMultipleFoodsDialogState {
    return remember {
        SelectMultipleFoodsDialogState()
    }
}

class SelectMultipleFoodsDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {

    val selectedFoods = mutableStateListOf<TandoorFood>()

    fun open(selectedFoods: List<TandoorFood>) {
        this.selectedFoods.clear()
        this.selectedFoods.addAll(selectedFoods)

        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@Composable
fun SelectMultipleFoodsDialog(
    state: SelectMultipleFoodsDialogState,
    prepend: @Composable () -> Unit,
    onSubmit: (foods: List<TandoorFood>) -> Unit
) {
    if(!state.shown.value) return

    AlertDialog(
        modifier = Modifier.fullWidthAlertDialogPadding(),
        onDismissRequest = {
            state.dismiss()
        },
        icon = {
            Icon(Icons.Rounded.RestaurantMenu, stringResource(Res.string.search_ingredients_filter))
        },
        title = {
            Text(stringResource(Res.string.search_ingredients_filter))
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
                                FoodSearchBar(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(),
                                    selectedFoods = state.selectedFoods
                                ) { food, value ->
                                    if(value) {
                                        state.selectedFoods.add(food)
                                    } else {
                                        state.selectedFoods.remove(food)
                                    }
                                }
                            }
                        }
                    ) {
                        Box(
                            Modifier.fillMaxHeight()
                        ) {
                            if(state.selectedFoods.size == 0) {
                                FullSizeAlertPane(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = stringResource(Res.string.search_ingredients_filter_empty),
                                    text = stringResource(Res.string.search_ingredients_filter_empty)
                                )
                            } else {
                                LazyColumn(
                                    Modifier.clip(RoundedCornerShape(16.dp))
                                ) {
                                    items(
                                        state.selectedFoods.size,
                                        key = { state.selectedFoods[it].id }) {
                                        val food = state.selectedFoods[it]

                                        FoodCheckedListItem(
                                            Modifier,
                                            checked = true,
                                            food = food
                                        ) {
                                            state.selectedFoods.remove(food)
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
                onSubmit(state.selectedFoods)
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
fun FoodCheckedListItem(
    modifier: Modifier = Modifier,
    checked: Boolean,
    food: TandoorFood,
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
            Text(food.name)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchBar(
    modifier: Modifier = Modifier,
    selectedFoods: List<TandoorFood>,
    onCheckedChange: (food: TandoorFood, value: Boolean) -> Unit
) {
    val foodRepo = koinInject<FoodRepo>()
    val keyboardController = LocalSoftwareKeyboardController.current

    var query by rememberSaveable { mutableStateOf("") }

    val searchResults = remember { mutableStateListOf<TandoorFood>() }
    LaunchedEffect(query) {
        if (query.isBlank()) {
            searchResults.clear()
            return@LaunchedEffect
        }
        foodRepo.search(query).collect { results ->
            searchResults.clear()
            searchResults.addAll(results)
        }
    }

    AlwaysDockedSearchBar(
        modifier = modifier,
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            dividerColor = Color.Transparent
        ),
        inputField = {
            SearchBarDefaults.InputFieldWithIOSKeyboardWorkaround(
                query = query,
                onQueryChange = { query = it },
                onSearch = {
                    keyboardController?.hide()
                },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Search,
                        stringResource(Res.string.search_ingredients)
                    )
                },
                placeholder = { Text(stringResource(Res.string.search_ingredients)) },
                expanded = true,
                onExpandedChange = { }
            )
        }
    ) {
        LazyColumn {
            items(searchResults.size, key = { searchResults[it].id }) {
                val food = searchResults[it]

                FoodCheckedListItem(
                    checked = selectedFoods.contains(food),
                    food = food
                ) {
                    keyboardController?.hide()
                    onCheckedChange(food, !selectedFoods.contains(food))
                }
            }
        }
    }
}