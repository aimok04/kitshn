package de.kitshn.ui.component.input

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.api.tandoor.model.TandoorMealType
import de.kitshn.ui.dialog.mealtype.MealTypeCreationAndEditDialog
import de.kitshn.ui.dialog.mealtype.rememberMealTypeCreationAndEditDialogState
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.action_create_meal_type
import kitshn.shared.generated.resources.common_unknown_meal_type
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BaseMealTypePickerField(
    client: TandoorClient,
    value: Int? = null,
    onValueChange: (Int?) -> Unit,
    content: @Composable (
        thumbnail: @Composable (() -> Unit)?,
        value: String,
        onClick: () -> Unit
    ) -> Unit
) {
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current

    var showMealTypePickerDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val mealTypeList = remember { mutableStateListOf<TandoorMealType>() }
    LaunchedEffect(Unit) {
        try {
            client.mealType.fetch().let { mealTypes ->
                mealTypeList.clear()
                mealTypeList.addAll(mealTypes.sortedWith(compareBy({ it.order }, { it.time })))
            }
        } catch(e: TandoorRequestsError) {
            Logger.e("MealTypePickerField.kt", e)
        }
    }
    val selectedMealType = mealTypeList.firstOrNull { it.id == value }

    val text = if (value == null) "" else (selectedMealType?.name ?: stringResource(Res.string.common_unknown_meal_type))
    content(
        if(selectedMealType != null) {
            {
                Box(
                    Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(selectedMealType.color)
                )
            }
        } else null,
        text
    ) {
        focus.clearFocus()
        showMealTypePickerDialog = true
    }

    val createAndEditDialogState = rememberMealTypeCreationAndEditDialogState(
        "MealTypePickerField/mealTypeCreationAndEditState"
    )

    MealTypeCreationAndEditDialog(
        client = client,
        state = createAndEditDialogState,
        onSaved = { savedMealType ->
            val index = mealTypeList.indexOfFirst { it.id == savedMealType.id }
            if (index >= 0) {
                mealTypeList[index] = savedMealType
            } else {
                mealTypeList.add(savedMealType)

                val sorted = mealTypeList.sortedWith(compareBy({ it.order }, { it.time }))
                mealTypeList.clear()
                mealTypeList.addAll(sorted)
            }

            onValueChange(savedMealType.id)
            showMealTypePickerDialog = false
        },
        onDeleted = { deletedMealType ->
            mealTypeList.removeAll { it.id == deletedMealType.id }
            if (selectedMealType?.id == deletedMealType.id) {
                onValueChange(null)
            }
        }
    )

    if(showMealTypePickerDialog) ModalBottomSheet(
        onDismissRequest = { showMealTypePickerDialog = false },
        sheetState = sheetState
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            itemsIndexed(mealTypeList) { index, mealType ->
                val isSelected = mealType.id == selectedMealType?.id

                SegmentedListItem(
                    selected = isSelected,
                    onClick = {
                        onValueChange(mealType.id)
                        scope.launch {
                            sheetState.hide()
                            showMealTypePickerDialog = false
                        }
                    },
                    onLongClick = {
                        createAndEditDialogState.edit(mealType)
                    },

                    shapes = ListItemDefaults.segmentedShapes(
                        index = index,
                        count = mealTypeList.size,
                        defaultShapes = ListItemDefaults.shapes(
                            shape = when(mealTypeList.size == 1) {
                                true -> RoundedCornerShape(16.dp)
                                else -> null
                            }
                        )
                    ),

                    content = {
                        Text(mealType.name)
                    },
                    leadingContent = {
                        Box(
                            Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(mealType.color)
                        )
                    },
                    trailingContent = {
                        if(isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }

            if(mealTypeList.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                }
            }

            item {
                SegmentedListItem(
                    onClick = {
                        createAndEditDialogState.create()
                    },

                    shapes = ListItemDefaults.segmentedShapes(
                        index = 0,
                        count = 1,
                        defaultShapes = ListItemDefaults.shapes(
                            shape = RoundedCornerShape(16.dp)
                        )
                    ),

                    content = {
                        Text(stringResource(Res.string.action_create_meal_type))
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(Res.string.action_create_meal_type)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun OutlinedMealTypePickerField(
    client: TandoorClient,
    value: Int? = null,
    onValueChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) = BaseMealTypePickerField(
    client = client,
    value = value,
    onValueChange = onValueChange
) { t, v, oc ->
    Box {
        OutlinedTextField(
            value = v,
            modifier = modifier
                .fillMaxWidth(),
            enabled = true,
            readOnly = true,
            singleLine = true,
            textStyle = textStyle,
            label = label,
            placeholder = placeholder,
            leadingIcon = t ?: leadingIcon,
            trailingIcon = trailingIcon ?: {
                Icon(
                    imageVector = Icons.Default.UnfoldMore,
                    contentDescription = null
                )
            },
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = isError,
            shape = shape,
            colors = colors,
            onValueChange = { }
        )

        Box(
            modifier = Modifier.matchParentSize().clickable { oc() }
        )
    }
}

@Composable
fun MealTypePickerField(
    client: TandoorClient,
    value: Int? = null,
    onValueChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors()
) = BaseMealTypePickerField(
    client = client,
    value = value,
    onValueChange = onValueChange
) { t, v, oc ->
    Box {
        TextField(
            value = v,
            modifier = modifier
                .fillMaxWidth(),
            enabled = true,
            readOnly = true,
            singleLine = true,
            textStyle = textStyle,
            label = label,
            placeholder = placeholder,
            leadingIcon = t ?: leadingIcon,
            trailingIcon = trailingIcon ?: {
                Icon(
                    imageVector = Icons.Default.UnfoldMore,
                    contentDescription = null
                )
            },
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = isError,
            shape = shape,
            colors = colors,
            onValueChange = { }
        )

        Box(
            modifier = Modifier.matchParentSize().clickable { oc() }
        )
    }
}
