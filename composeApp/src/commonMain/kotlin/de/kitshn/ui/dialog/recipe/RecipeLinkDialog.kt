package de.kitshn.ui.dialog.recipe

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.ui.component.buttons.BackButton
import de.kitshn.ui.component.buttons.BackButtonType
import de.kitshn.ui.dialog.AdaptiveFullscreenDialog
import de.kitshn.ui.view.ViewParameters
import de.kitshn.ui.view.recipe.details.ViewRecipeDetails
import kotlinx.coroutines.delay

@Composable
fun rememberRecipeLinkDialogState(): RecipeLinkDialogState {
    return remember {
        RecipeLinkDialogState()
    }
}

class RecipeLinkDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false),
    val linkContent: MutableState<TandoorRecipeOverview?> = mutableStateOf(null)
) {
    var overrideServings: Double? = null

    fun open(linkContent: TandoorRecipeOverview, overrideServings: Double? = null) {
        this.linkContent.value = linkContent
        this.shown.value = true

        this.overrideServings = overrideServings
    }

    fun dismiss() {
        this.shown.value = false
        this.linkContent.value = null
    }
}

@Composable
fun RecipeLinkDialog(
    p: ViewParameters,
    state: RecipeLinkDialogState,
    leadingContent: @Composable () -> Unit = {},
    bottomBar: @Composable ((isFullscreen: Boolean) -> Unit)? = {},
    offsetFab: Boolean = false,
    onServingsChange: (servings: Double) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    LaunchedEffect(
        state.linkContent.value
    ) {
        if(state.linkContent.value == null) return@LaunchedEffect
        if(p.vm.tandoorClient?.container?.recipeOverview?.contains(state.linkContent.value?.id) == true) return@LaunchedEffect

        state.linkContent.value?.id?.let {
            p.vm.tandoorClient?.container?.recipeOverview?.put(
                it,
                state.linkContent.value
            )
        }
    }

    /* workaround */
    var forceDismiss by remember { mutableStateOf(false) }
    LaunchedEffect(forceDismiss) {
        delay(100)
        forceDismiss = false
    }

    if(state.linkContent.value != null) AdaptiveFullscreenDialog(
        onDismiss = {
            state.dismiss()
            onDismiss()
        },
        forceDismiss = forceDismiss,
        title = { },
        topBar = { },
        topBarWrapper = { },
        bottomBar = bottomBar,
        applyPaddingValues = false,
    ) { _, isFullscreen, pv ->
        ViewRecipeDetails(
            p = p,

            client = p.vm.tandoorClient,
            recipeId = state.linkContent.value?.id?: -1,

            prependContent = leadingContent,

            overrideServings = state.overrideServings,

            navigationIcon = {
                BackButton(
                    onBack = {
                        forceDismiss = true
                    },
                    overlay = true,
                    type = BackButtonType.CLOSE
                )
            },

            overridePaddingValues = pv,
            contentWindowInsets = if(isFullscreen)
                ScaffoldDefaults.contentWindowInsets
            else
                WindowInsets(),
            offsetFab = isFullscreen && offsetFab,

            onClickKeyword = {
                p.vm.searchKeyword(it.id)
                state.dismiss()
            },
            onClickUser = {
                p.vm.searchCreatedBy(it.id)
                state.dismiss()
            },
            onServingsChange = {
                onServingsChange(it)
            }
        )
    }
}