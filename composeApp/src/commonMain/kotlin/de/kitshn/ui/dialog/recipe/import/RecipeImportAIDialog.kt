package de.kitshn.ui.dialog.recipe.import

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.eygraber.uri.Uri
import de.kitshn.FileFormats
import de.kitshn.KitshnViewModel
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.api.tandoor.route.TandoorAIImportRoute
import de.kitshn.handleTandoorRequestState
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.HorizontalDividerWithLabel
import de.kitshn.ui.component.icons.IconWithState
import de.kitshn.ui.dialog.AdaptiveFullscreenDialog
import de.kitshn.ui.dialog.peekaboo.PhotoTakingDialog
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_continue
import kitshn.composeapp.generated.resources.action_import
import kitshn.composeapp.generated.resources.action_take_photo
import kitshn.composeapp.generated.resources.action_upload
import kitshn.composeapp.generated.resources.common_or_upper
import kitshn.composeapp.generated.resources.common_recipe
import kitshn.composeapp.generated.resources.recipe_import_type_ai_label
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

class RecipeImportAIDialogStateData {
    var file by mutableStateOf<TandoorAIImportRoute.File?>(null)
    var text by mutableStateOf("")
}

@Composable
fun rememberRecipeImportAIDialogState(): RecipeImportAIDialogState {
    return remember {
        RecipeImportAIDialogState()
    }
}

class RecipeImportAIDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {
    var data = RecipeImportCommonStateData()
    var additionalData = RecipeImportAIDialogStateData()

    fun open() {
        this.data = RecipeImportCommonStateData()
        this.additionalData = RecipeImportAIDialogStateData()
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RecipeImportAIDialog(
    vm: KitshnViewModel,
    state: RecipeImportAIDialogState,
    onViewRecipe: (recipe: TandoorRecipe) -> Unit = { }
) {
    val client = vm.tandoorClient ?: return

    if(!state.shown.value) return

    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    val fetchRequestState = rememberTandoorRequestState()
    fun fetch() = coroutineScope.launch {
        fetchRequestState.wrapRequest {
            val response = client.aiImport.fetch(
                file = state.additionalData.file,
                text = state.additionalData.text.ifBlank { null }
            )

            if(response.link != null) {
                val uri = Uri.parse(response.link)
                val pathArgs = (uri.path ?: "").split("/").toMutableList().apply {
                    removeFirstOrNull()
                }

                if(pathArgs.size > 2 && pathArgs[0] == "view" && pathArgs[1] == "recipe") {
                    state.dismiss()
                    vm.viewRecipe(pathArgs[2].toInt())
                }
            } else if(response.recipeFromSource != null) {
                state.data.recipeFromSource = response.recipeFromSource
                state.data.populate()
            }
        }

        hapticFeedback.handleTandoorRequestState(fetchRequestState)
    }

    var showPhotoTakingDialog by remember { mutableStateOf(false) }
    val filePickerLauncher = rememberFilePickerLauncher { file ->
        if(file == null) return@rememberFilePickerLauncher

        coroutineScope.launch {
            state.additionalData.file = TandoorAIImportRoute.File(
                name = file.name,
                byteArray = file.readBytes(),
                mimeType = FileFormats.findMimeType(file.extension.lowercase()) ?: "unknown"
            )
            state.additionalData.text = ""
            fetch()
        }
    }

    val recipeImportRequestState = rememberTandoorRequestState()

    AdaptiveFullscreenDialog(
        onDismiss = { state.dismiss() },
        title = { Text(text = stringResource(Res.string.recipe_import_type_ai_label)) },
        topAppBarActions = {
            if(state.data.recipeFromSource != null) FilledIconButton(
                onClick = {
                    coroutineScope.launch {
                        if(recipeImportRequestState.state == TandoorRequestStateState.LOADING) return@launch
                        recipeImportRequestState.wrapRequest {
                            state.data.import(
                                onViewRecipe = onViewRecipe,
                                onDismiss = {
                                    state.dismiss()
                                }
                            )
                        }

                        hapticFeedback.handleTandoorRequestState(recipeImportRequestState)
                    }
                }
            ) {
                IconWithState(
                    progressIndicatorTint = LocalContentColor.current,
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(Res.string.action_import),
                    state = recipeImportRequestState.state.toIconWithState()
                )
            }
        },
        maxWidth = 600.dp
    ) { nsc, _, _ ->
        Column {
            LinearWavyProgressIndicator(
                Modifier
                    .alpha(if(fetchRequestState.state == TandoorRequestStateState.LOADING) 1f else 0f)
                    .fillMaxWidth()
            )

            BoxWithConstraints {
                val containerSize = ((maxHeight - 350.dp) / 2).coerceAtLeast(205.dp)
                val maxTextFieldHeight = (maxHeight - 96.dp).coerceAtLeast(200.dp)

                LazyColumn(
                    Modifier.nestedScroll(nsc)
                ) {
                    if(state.data.recipeFromSource == null) {
                        item {
                            if(state.additionalData.text.isEmpty()) Row(
                                Modifier.padding(16.dp)
                            ) {
                                OutlinedButton(
                                    modifier = Modifier.fillMaxWidth(0.5f),
                                    onClick = {
                                        filePickerLauncher.launch()
                                    }
                                ) {
                                    Icon(
                                        Icons.Rounded.UploadFile,
                                        stringResource(Res.string.action_upload)
                                    )

                                    Spacer(Modifier.width(8.dp))

                                    Text(stringResource(Res.string.action_upload))
                                }

                                Spacer(Modifier.width(8.dp))

                                OutlinedButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        showPhotoTakingDialog = true
                                    }
                                ) {
                                    Icon(
                                        Icons.Rounded.Camera,
                                        stringResource(Res.string.action_take_photo)
                                    )

                                    Spacer(Modifier.width(8.dp))

                                    Text(stringResource(Res.string.action_take_photo))
                                }
                            }
                        }

                        item {
                            if(state.additionalData.text.isEmpty()) Box(
                                Modifier.padding(start = 16.dp, end = 16.dp)
                            ) {
                                HorizontalDividerWithLabel(
                                    text = stringResource(Res.string.common_or_upper)
                                )
                            }
                        }

                        item {
                            TextField(
                                value = state.additionalData.text,
                                onValueChange = {
                                    state.additionalData.text = it
                                },

                                modifier = Modifier
                                    .padding(
                                        top = 16.dp,
                                        start = 16.dp,
                                        end = 16.dp
                                    )
                                    .fillMaxWidth()
                                    .heightIn(max = maxTextFieldHeight),

                                label = {
                                    Text(text = stringResource(Res.string.common_recipe))
                                }
                            )

                            Button(
                                modifier = Modifier
                                    .padding(
                                        top = 8.dp,
                                        start = 16.dp,
                                        end = 16.dp,
                                        bottom = 16.dp
                                    )
                                    .fillMaxWidth(),
                                onClick = {
                                    fetch()
                                }
                            ) {
                                Text(text = stringResource(Res.string.action_continue))
                            }
                        }
                    }

                    RecipeImportCommon(
                        fetchRequestState = fetchRequestState,
                        data = state.data,
                        containerSize = containerSize,
                        displayDivider = false
                    )
                }
            }
        }
    }

    PhotoTakingDialog(
        shown = showPhotoTakingDialog,
        onSelect = {
            state.additionalData.file = TandoorAIImportRoute.File(
                name = "image.png",
                byteArray = it,
                mimeType = "image/png"
            )
            state.additionalData.text = ""
            fetch()
        }
    ) {
        showPhotoTakingDialog = false
    }

    TandoorRequestErrorHandler(state = recipeImportRequestState)
}