package de.kitshn.android.ui.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import de.kitshn.android.KitshnViewModel
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.model.TandoorKeywordOverview
import de.kitshn.android.ui.view.ViewParameters
import de.kitshn.android.ui.view.recipe.details.ViewRecipeDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitshnRecipeListRecipeDetailPaneScaffold(
    vm: KitshnViewModel,
    key: String,
    topBar: @Composable (colors: TopAppBarColors) -> Unit,
    floatingActionButton: @Composable () -> Unit = {},
    onClickKeyword: (keyword: TandoorKeywordOverview) -> Unit = {},
    listContent: @Composable (pv: PaddingValues, selectedId: String?, supportsMultiplePages: Boolean, background: Color, select: (id: String) -> Unit) -> Unit,
) {
    KitshnListDetailPaneScaffold(
        key = key,
        topBar = topBar,
        floatingActionButton = floatingActionButton,
        listContent = listContent
    ) { selectId, supportsMultiplePanes, expandDetailPane, toggleExpandedDetailPane, back ->
        LaunchedEffect(selectId) {
            if(vm.tandoorClient?.container?.recipeOverview?.containsKey(selectId.toInt()) == true) return@LaunchedEffect
            vm.tandoorClient?.container?.recipeOverview?.put(
                selectId.toInt(),
                vm.tandoorClient?.recipe?.get(selectId.toInt())?.toOverview()
            )
        }

        ViewRecipeDetails(
            p = ViewParameters(vm, back),

            navigationIcon = if(supportsMultiplePanes || expandDetailPane) {
                {
                    FilledIconButton(
                        onClick = toggleExpandedDetailPane,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        when(expandDetailPane) {
                            true -> Icon(
                                Icons.Rounded.FullscreenExit,
                                stringResource(R.string.action_expand_less)
                            )

                            else -> Icon(
                                Icons.Rounded.Fullscreen,
                                stringResource(R.string.expand_more)
                            )
                        }
                    }
                }
            } else {
                null
            },

            recipeId = selectId.toInt(),
            client = vm.tandoorClient,

            onClickKeyword = { keyword ->
                back?.let { it() }
                onClickKeyword(keyword)
            }
        )
    }
}