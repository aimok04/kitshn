package de.kitshn.ui.dialog.recipe.import

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.eygraber.uri.Uri
import de.kitshn.KitshnViewModel
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.handleTandoorRequestState
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.icons.IconWithState
import de.kitshn.ui.dialog.AdaptiveFullscreenDialog
import de.kitshn.ui.state.foreverRememberNotSavable
import kitshn.composeApp.BuildConfig
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_download
import kitshn.composeapp.generated.resources.action_import
import kitshn.composeapp.generated.resources.common_recipe_url
import kitshn.composeapp.generated.resources.error_recipe_could_not_be_loaded
import kitshn.composeapp.generated.resources.recipe_import_type_url_label
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberRecipeImportUrlDialogState(
    key: String
): RecipeImportUrlDialogState {
    val value by foreverRememberNotSavable(
        key = key,
        initialValue = RecipeImportUrlDialogState()
    )

    return value
}

class RecipeImportUrlDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {
    var data = RecipeImportCommonStateData()

    var autoFetch = false

    fun open(url: String = "", autoFetch: Boolean = false) {
        this.data = RecipeImportCommonStateData()

        this.autoFetch = autoFetch
        this.shown.value = true

        this.data.url = url
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RecipeImportUrlDialog(
    vm: KitshnViewModel,
    state: RecipeImportUrlDialogState,
    onViewRecipe: (recipe: TandoorRecipe) -> Unit = { },
    onSocialMediaImport: (url: String) -> Unit = { }
) {
    val client = vm.tandoorClient ?: return

    if(!state.shown.value) {
        LaunchedEffect(Unit) {
            delay(2000)
            IMPORT_URI_BLACKLIST.clear()
        }

        return
    }

    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val hapticFeedback = LocalHapticFeedback.current

    val fetchRequestState = rememberTandoorRequestState()
    fun fetch() = coroutineScope.launch {
        val url = state.data.url.replaceFirst(BuildConfig.SHARE_WRAPPER_URL, "")

        // detect social media import domain and forward to RecipeImportSocialMediaDialog
        val uri = Uri.parse(url)
        if(SOCIAL_MEDIA_IMPORT_DOMAINS.contains(uri.host)) {
            state.dismiss()
            onSocialMediaImport(url)
            return@launch
        }

        fetchRequestState.wrapRequest {
            val response = client.recipeFromSource.fetch(url)
            if(response.recipe == null && response.recipeId != null) {
                state.dismiss()
                vm.viewRecipe(response.recipeId)
                return@wrapRequest
            }

            if(response.error) {
                throw TandoorRequestsError(
                    null,
                    null,
                    overrideMessage = response.msg
                )
            }

            state.data.recipeFromSource = response
            state.data.populate()
        }

        hapticFeedback.handleTandoorRequestState(fetchRequestState)
    }

    LaunchedEffect(state.autoFetch) {
        if(!state.autoFetch) return@LaunchedEffect
        state.autoFetch = false

        fetch()
    }

    val recipeImportRequestState = rememberTandoorRequestState()

    AdaptiveFullscreenDialog(
        onDismiss = { state.dismiss() },
        title = { Text(text = stringResource(Res.string.recipe_import_type_url_label)) },
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

                LazyColumn(
                    Modifier.nestedScroll(nsc)
                ) {
                    item {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextField(
                                modifier = Modifier
                                    .weight(1f, true)
                                    .focusRequester(focusRequester),

                                value = state.data.url,
                                label = { Text(text = stringResource(Res.string.common_recipe_url)) },

                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.Receipt,
                                        stringResource(Res.string.common_recipe_url)
                                    )
                                },

                                singleLine = true,

                                keyboardActions = KeyboardActions(onGo = { fetch() }),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),

                                isError = fetchRequestState.state == TandoorRequestStateState.ERROR,
                                supportingText = {
                                    if(fetchRequestState.state == TandoorRequestStateState.ERROR)
                                        Text(text = "${stringResource(Res.string.error_recipe_could_not_be_loaded)}: ${fetchRequestState.error?.message}")
                                },

                                onValueChange = { state.data.url = it }
                            )

                            IconButton(
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(),
                                onClick = { fetch() }
                            ) {
                                Icon(
                                    Icons.Rounded.Download,
                                    stringResource(Res.string.action_download)
                                )
                            }

                            LaunchedEffect(Unit) {
                                delay(500)

                                try {
                                    this.coroutineContext.job.invokeOnCompletion {
                                        focusRequester.requestFocus()
                                    }
                                } catch(e: Exception) {
                                    Logger.e("RecipeImportUrlDialog.kt", e)
                                }
                            }
                        }
                    }

                    RecipeImportCommon(
                        fetchRequestState = fetchRequestState,
                        data = state.data,
                        containerSize = containerSize
                    )
                }
            }
        }
    }

    TandoorRequestErrorHandler(state = recipeImportRequestState)
}