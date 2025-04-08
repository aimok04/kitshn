package de.kitshn.ui.dialog.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import com.eygraber.uri.Uri
import de.kitshn.KitshnViewModel
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeFromSource
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.icons.IconWithState
import de.kitshn.ui.component.settings.SettingsSwitchListItem
import de.kitshn.ui.dialog.AdaptiveFullscreenDialog
import de.kitshn.ui.state.foreverRememberNotSavable
import de.kitshn.ui.theme.Typography
import kitshn.composeApp.BuildConfig
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_download
import kitshn.composeapp.generated.resources.action_import
import kitshn.composeapp.generated.resources.common_import_recipe
import kitshn.composeapp.generated.resources.common_recipe_url
import kitshn.composeapp.generated.resources.common_selected
import kitshn.composeapp.generated.resources.common_steps
import kitshn.composeapp.generated.resources.common_tags
import kitshn.composeapp.generated.resources.common_title_image
import kitshn.composeapp.generated.resources.error_recipe_could_not_be_loaded
import kitshn.composeapp.generated.resources.recipe_import_divide_steps
import kitshn.composeapp.generated.resources.recipe_import_divide_steps_description
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberRecipeImportDialogState(
    key: String
): RecipeImportDialogState {
    val value by foreverRememberNotSavable(
        key = key,
        initialValue = RecipeImportDialogState()
    )

    return value
}

class RecipeImportDialogStateData {

    var url by mutableStateOf("")
    var recipeFromSource by mutableStateOf<TandoorRecipeFromSource?>(null)

    val availableImageUrls = mutableStateListOf<String>()
    var selectedImageUrl by mutableStateOf("")

    val selectedKeywords = mutableStateListOf<String>()

    var splitSteps by mutableStateOf(true)

    fun populate() {
        availableImageUrls.clear()
        availableImageUrls.add(recipeFromSource!!.recipe.imageUrl)
        availableImageUrls.addAll(recipeFromSource!!.images)

        selectedImageUrl = availableImageUrls.getOrNull(0) ?: ""

        selectedKeywords.clear()
        selectedKeywords.addAll(recipeFromSource!!.recipe.keywords.map { it.name ?: "" })
    }

}

class RecipeImportDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {
    var data = RecipeImportDialogStateData()

    var autoFetch = false

    fun open(url: String = "", autoFetch: Boolean = false) {
        this.data = RecipeImportDialogStateData()

        this.autoFetch = autoFetch
        this.shown.value = true

        this.data.url = url
    }

    fun dismiss() {
        this.shown.value = false
    }
}

// ensure recipe gets imported only once (issue #29)
val IMPORT_URI_BLACKLIST = mutableListOf<String>()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeImportDialog(
    vm: KitshnViewModel,
    state: RecipeImportDialogState,
    onViewRecipe: (recipe: TandoorRecipe) -> Unit = { }
) {
    val context = LocalPlatformContext.current
    val imageLoader = remember { ImageLoader(context) }

    val client = vm.tandoorClient ?: return

    // handle import recipe url passing
    vm.uiState.importRecipeUrl.WatchAndConsume {
        state.dismiss()
        delay(50)
        state.open(url = it, autoFetch = true)
    }

    if(!state.shown.value) {
        LaunchedEffect(Unit) {
            delay(2000)
            IMPORT_URI_BLACKLIST.clear()
        }

        return
    }

    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    val fetchRequestState = rememberTandoorRequestState()
    fun fetch() = coroutineScope.launch {
        val url = state.data.url.replaceFirst(BuildConfig.SHARE_WRAPPER_URL, "")

        fetchRequestState.wrapRequest {
            val response = client.recipeFromSource.fetch(url)
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
    }

    LaunchedEffect(state.autoFetch) {
        if(!state.autoFetch) return@LaunchedEffect
        state.autoFetch = false

        fetch()
    }

    val recipeImportRequestState = rememberTandoorRequestState()
    fun import() = coroutineScope.launch {
        if(recipeImportRequestState.state == TandoorRequestStateState.LOADING) return@launch

        recipeImportRequestState.wrapRequest {
            // ensure recipe gets imported only once (issue #29)
            if(IMPORT_URI_BLACKLIST.contains(state.data.url)) return@wrapRequest
            IMPORT_URI_BLACKLIST.add(state.data.url)

            val recipe = state.data.recipeFromSource!!.create(
                imageUrl = state.data.selectedImageUrl,
                keywords = state.data.selectedKeywords,
                splitSteps = state.data.splitSteps
            )

            recipe.setImageUrl(state.data.selectedImageUrl)

            state.dismiss()
            onViewRecipe(recipe)
        }
    }

    AdaptiveFullscreenDialog(
        onDismiss = { state.dismiss() },
        title = { Text(text = stringResource(Res.string.common_import_recipe)) },
        topAppBarActions = {
            if(state.data.recipeFromSource != null) FilledIconButton(
                onClick = { import() }
            ) {
                IconWithState(
                    progressIndicatorTint = LocalContentColor.current,
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(Res.string.action_import),
                    state = recipeImportRequestState.state.toIconWithState()
                )
            }
        }
    ) { nsc, _, _ ->
        Column {
            LinearProgressIndicator(
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
                                        Text(text = stringResource(Res.string.error_recipe_could_not_be_loaded))
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
                                try {
                                    this.coroutineContext.job.invokeOnCompletion {
                                        focusRequester.requestFocus()
                                    }
                                } catch(e: Exception) {
                                    Logger.e("RecipeImportDialog.kt", e)
                                }
                            }
                        }
                    }

                    if(fetchRequestState.state == TandoorRequestStateState.SUCCESS && state.data.recipeFromSource != null) {
                        item {
                            HorizontalDivider(
                                Modifier.padding(start = 16.dp, end = 16.dp)
                            )
                        }

                        item {
                            Text(
                                text = stringResource(Res.string.common_title_image),
                                Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                                style = Typography().titleLarge
                            )
                        }

                        item {
                            HorizontalMultiBrowseCarousel(
                                state = rememberCarouselState { state.data.availableImageUrls.size },
                                preferredItemWidth = 250.dp,
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 8.dp
                                )
                            ) {
                                val url = state.data.availableImageUrls[it]

                                Box(
                                    modifier = Modifier
                                        .height(containerSize)
                                ) {
                                    AsyncImage(
                                        modifier = Modifier
                                            .height(containerSize)
                                            .maskClip(MaterialTheme.shapes.extraLarge)
                                            .clickable { state.data.selectedImageUrl = url },
                                        model = url,
                                        contentDescription = url,
                                        contentScale = ContentScale.Crop,
                                        imageLoader = imageLoader
                                    )

                                    if(state.data.selectedImageUrl == url) Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(containerSize)
                                            .maskClip(MaterialTheme.shapes.extraLarge)
                                            .background(Color.Black.copy(alpha = 0.3f))
                                            .clickable { state.data.selectedImageUrl = url },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = stringResource(Res.string.common_selected),
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = stringResource(Res.string.common_tags),
                                Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                                style = Typography().titleLarge
                            )
                        }

                        item {
                            LazyColumn(
                                Modifier
                                    .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                                    .clip(MaterialTheme.shapes.extraLarge)
                                    .height(containerSize)
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                val keywords = state.data.recipeFromSource!!.recipe.keywords

                                items(keywords.size) {
                                    val keyword = keywords[it]
                                    val checked = state.data.selectedKeywords.contains(keyword.name)

                                    fun toggle() {
                                        if(checked) {
                                            state.data.selectedKeywords.remove(keyword.name)
                                        } else {
                                            state.data.selectedKeywords.add(keyword.name ?: "")
                                        }
                                    }

                                    ListItem(
                                        modifier = Modifier
                                            .clickable { toggle() },
                                        colors = ListItemDefaults.colors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                                        ),
                                        leadingContent = {
                                            Checkbox(
                                                checked = checked,
                                                onCheckedChange = { toggle() }
                                            )
                                        },
                                        headlineContent = {
                                            Text(keyword.label ?: "")
                                        }
                                    )
                                }
                            }
                        }

                        item {
                            Text(
                                text = stringResource(Res.string.common_steps),
                                Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                                style = Typography().titleLarge
                            )
                        }

                        item {
                            SettingsSwitchListItem(
                                label = { Text(text = stringResource(Res.string.recipe_import_divide_steps)) },
                                description = { Text(text = stringResource(Res.string.recipe_import_divide_steps_description)) },
                                icon = Icons.Rounded.Compress,
                                contentDescription = stringResource(Res.string.recipe_import_divide_steps),
                                checked = state.data.splitSteps
                            ) {
                                state.data.splitSteps = it
                            }
                        }
                    }
                }
            }
        }
    }

    TandoorRequestErrorHandler(state = recipeImportRequestState)
}