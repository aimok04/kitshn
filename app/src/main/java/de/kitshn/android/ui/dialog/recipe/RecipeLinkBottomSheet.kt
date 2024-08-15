package de.kitshn.android.ui.dialog.recipe

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.android.ui.view.ViewParameters
import de.kitshn.android.ui.view.recipe.details.ViewRecipeDetails

@Composable
fun rememberRecipeLinkBottomSheetState(): RecipeLinkBottomSheetState {
    return remember {
        RecipeLinkBottomSheetState()
    }
}

class RecipeLinkBottomSheetState(
    val shown: MutableState<Boolean> = mutableStateOf(false),
    val linkContent: MutableState<TandoorRecipeOverview?> = mutableStateOf(null)
) {
    var overrideServings: Int? = null

    fun open(linkContent: TandoorRecipeOverview, overrideServings: Int? = null) {
        this.linkContent.value = linkContent
        this.shown.value = true

        this.overrideServings = overrideServings
    }

    fun dismiss() {
        this.shown.value = false
        this.linkContent.value = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeLinkBottomSheet(
    p: ViewParameters,
    state: RecipeLinkBottomSheetState,
    leadingContent: @Composable () -> Unit = {},
    dragHandle: @Composable () -> Unit = { BottomSheetDefaults.DragHandle() },
    onDismiss: () -> Unit = {}
) {
    val density = LocalDensity.current
    val modalBottomSheetState = rememberModalBottomSheetState()

    LaunchedEffect(
        state.shown.value
    ) {
        if(state.shown.value) {
            modalBottomSheetState.show()
        } else {
            modalBottomSheetState.hide()
        }
    }

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

    if(state.linkContent.value != null) ModalBottomSheet(
        modifier = Modifier.padding(
            top = with(density) {
                WindowInsets.statusBars
                    .getTop(density)
                    .toDp() * 2
            }
        ),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        onDismissRequest = {
            state.dismiss()
            onDismiss()
        },
        sheetState = modalBottomSheetState,
        dragHandle = dragHandle
    ) {
        leadingContent()

        Box(
            Modifier
                .padding(16.dp)
                .clip(
                    RoundedCornerShape(16.dp)
                )
        ) {
            ViewRecipeDetails(
                p = p,

                client = p.vm.tandoorClient,
                recipeId = state.linkContent.value!!.id,
                dialogMode = true,

                overrideServings = state.overrideServings,

                onClickKeyword = {
                    state.dismiss()
                    p.vm.searchKeyword(it.id)
                }
            )
        }
    }
}