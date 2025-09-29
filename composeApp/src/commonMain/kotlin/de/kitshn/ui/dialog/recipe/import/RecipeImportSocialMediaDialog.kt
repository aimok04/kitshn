package de.kitshn.ui.dialog.recipe.import

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Flare
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.placeholder
import com.eygraber.compose.placeholder.shimmer
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import de.kitshn.KitshnViewModel
import de.kitshn.api.import.runSocialMediaImportScript
import de.kitshn.api.tandoor.TandoorRequestState
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.api.tandoor.route.TandoorAIProvider
import de.kitshn.handleTandoorRequestState
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.icons.IconWithState
import de.kitshn.ui.dialog.AdaptiveFullscreenDialog
import de.kitshn.ui.dialog.select.SelectAIProviderDialog
import de.kitshn.ui.dialog.select.rememberSelectAIProviderDialogState
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_download
import kitshn.composeapp.generated.resources.action_import
import kitshn.composeapp.generated.resources.common_ai_provider
import kitshn.composeapp.generated.resources.common_recipe_url
import kitshn.composeapp.generated.resources.error_recipe_could_not_be_loaded
import kitshn.composeapp.generated.resources.recipe_import_type_social_media_disclaimer
import kitshn.composeapp.generated.resources.recipe_import_type_social_media_label
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

val SOCIAL_MEDIA_IMPORT_DOMAINS = listOf(
    "instagram.com",
    "www.instagram.com",
    "tiktok.com",
    "vm.tiktok.com",
    "www.tiktok.com"
)

@Composable
fun rememberRecipeImportSocialMediaDialogState(): RecipeImportSocialMediaDialogState {
    return remember {
        RecipeImportSocialMediaDialogState()
    }
}

class RecipeImportSocialMediaDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {
    var data = RecipeImportCommonStateData()
    var recipeDescription by mutableStateOf("")

    var autoFetch = false

    fun open(url: String = "", autoFetch: Boolean = false) {
        this.data = RecipeImportCommonStateData()

        this.autoFetch = autoFetch
        this.shown.value = true

        this.data.url = url
    }

    fun open() {
        this.data = RecipeImportCommonStateData()
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RecipeImportSocialMediaDialog(
    vm: KitshnViewModel,
    state: RecipeImportSocialMediaDialogState,
    onViewRecipe: (recipe: TandoorRecipe) -> Unit = { }
) {
    val client = vm.tandoorClient ?: return

    if(!state.shown.value) return

    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val hapticFeedback = LocalHapticFeedback.current

    val selectAIProviderDialogState = rememberSelectAIProviderDialogState()
    var aiProvider by remember { mutableStateOf<TandoorAIProvider?>(null) }

    LaunchedEffect(Unit) {
        TandoorRequestState().wrapRequest {
            val space = vm.tandoorClient!!.space.current()
            aiProvider = space.ai_default_provider
        }
    }

    val fetchAiRequestState = rememberTandoorRequestState()
    fun fetchAi() = coroutineScope.launch {
        fetchAiRequestState.wrapRequest {
            if(state.recipeDescription.length <= 3)
                throw Error("Recipe description has to be longer than three characters.")

            val response = client.aiImport.fetch(
                file = null,
                text = state.recipeDescription,
                aiProviderId = aiProvider?.id ?: -1
            )
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

        hapticFeedback.handleTandoorRequestState(fetchAiRequestState)
    }

    var displayWebView by remember { mutableStateOf(false) }
    val webViewNavigator = rememberWebViewNavigator()
    val webViewState = rememberWebViewState(url = state.data.url).apply {
        val userAgentBuilder = StringBuilder()
            .append("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36")

        if(platformDetails.platform == Platforms.IOS) {
            userAgentBuilder.append(" ")
            userAgentBuilder.append("kitshnWebKit")
        }

        webSettings.customUserAgentString = userAgentBuilder.toString()
        webSettings.supportZoom = false
        webSettings.androidWebSettings.domStorageEnabled = true
    }

    val fetchWebsiteRequestState = rememberTandoorRequestState()
    LaunchedEffect(webViewState.loadingState) {
        if(webViewState.loadingState != LoadingState.Finished) return@LaunchedEffect

        delay(2000)

        fetchWebsiteRequestState.wrapRequest {
            val response = webViewNavigator.runSocialMediaImportScript()

            if(response.imageURL != null) {
                val httpClient = HttpClient {
                    followRedirects = true
                }

                val imageBytes = httpClient.get(response.imageURL).bodyAsBytes()
                state.data.uploadImage = imageBytes
            }

            state.recipeDescription = response.description ?: ""
            fetchAi()
        }

        displayWebView = false
        hapticFeedback.handleTandoorRequestState(fetchWebsiteRequestState)
    }

    fun fetchWebsite() {
        displayWebView = true
    }
    LaunchedEffect(state.autoFetch) {
        if(!state.autoFetch) return@LaunchedEffect
        state.autoFetch = false

        fetchWebsite()
    }

    val recipeImportRequestState = rememberTandoorRequestState()

    AdaptiveFullscreenDialog(
        onDismiss = { state.dismiss() },
        title = { Text(text = stringResource(Res.string.recipe_import_type_social_media_label)) },
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
        bottomBar = {
            BottomAppBar(
                contentPadding = PaddingValues(8.dp)
            ) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),

                    readOnly = true,

                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Flare,
                            stringResource(Res.string.common_ai_provider)
                        )
                    },
                    label = { Text(text = stringResource(Res.string.common_ai_provider)) },
                    value = aiProvider?.name ?: "",

                    interactionSource = remember { MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect {
                                    if(it !is FocusInteraction.Focus && it !is PressInteraction.Release) return@collect
                                    selectAIProviderDialogState.open(
                                        aiProvider
                                    )
                                }
                            }
                        },

                    onValueChange = { }
                )
            }
        },
        maxWidth = 600.dp
    ) { nsc, _, _ ->
        when(displayWebView) {
            false -> Column {
                LinearWavyProgressIndicator(
                    Modifier
                        .alpha(if(fetchAiRequestState.state == TandoorRequestStateState.LOADING) 1f else 0f)
                        .fillMaxWidth()
                )

                BoxWithConstraints {
                    val containerSize = ((maxHeight - 350.dp) / 2).coerceAtLeast(205.dp)

                    LazyColumn(
                        Modifier.nestedScroll(nsc)
                    ) {
                        if(state.data.recipeFromSource == null) {
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

                                        keyboardActions = KeyboardActions(onGo = { fetchWebsite() }),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),

                                        isError = fetchWebsiteRequestState.state == TandoorRequestStateState.ERROR,
                                        supportingText = {
                                            Text(
                                                text = when(fetchWebsiteRequestState.state) {
                                                    TandoorRequestStateState.ERROR -> "${
                                                        stringResource(
                                                            Res.string.error_recipe_could_not_be_loaded
                                                        )
                                                    }: ${fetchAiRequestState.error?.message}"

                                                    else -> stringResource(Res.string.recipe_import_type_social_media_disclaimer)
                                                }
                                            )
                                        },

                                        onValueChange = { state.data.url = it }
                                    )

                                    IconButton(
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(),
                                        onClick = { fetchWebsite() }
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
                                            Logger.e("RecipeImportSocialMediaDialog.kt", e)
                                        }
                                    }
                                }
                            }
                        }

                        RecipeImportCommon(
                            fetchRequestState = fetchAiRequestState,
                            data = state.data,
                            containerSize = containerSize,
                            displayDivider = false
                        )
                    }
                }
            }

            true -> Box {
                WebView(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    state = webViewState,
                    navigator = webViewNavigator
                )

                Box(
                    Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { }
                        .placeholder(
                            visible = true,
                            color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                            highlight = PlaceholderHighlight.shimmer(
                                highlightColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                    alpha = 0.9f
                                )
                            )
                        )
                )
            }
        }
    }

    SelectAIProviderDialog(
        client = client,
        state = selectAIProviderDialogState
    ) {
        aiProvider = it
        hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
    }

    TandoorRequestErrorHandler(state = fetchWebsiteRequestState)
    TandoorRequestErrorHandler(state = recipeImportRequestState)
}