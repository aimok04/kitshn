package de.kitshn.ui.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import de.kitshn.KitshnViewModel
import de.kitshn.TestTagRepository
import de.kitshn.api.tandoor.TandoorRequestState
import de.kitshn.api.tandoor.model.TandoorKeywordOverview
import de.kitshn.api.tandoor.route.TandoorUser
import de.kitshn.ui.view.ViewParameters
import de.kitshn.ui.view.recipe.details.ViewRecipeDetails
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_close
import kitshn.composeapp.generated.resources.action_expand_less
import kitshn.composeapp.generated.resources.expand_more
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitshnRecipeListRecipeDetailPaneScaffold(
    modifier: Modifier = Modifier,
    vm: KitshnViewModel,
    key: String,
    topBar: @Composable (colors: TopAppBarColors) -> Unit,
    floatingActionButton: @Composable () -> Unit = {},
    onClickUser: (user: TandoorUser) -> Unit = {},
    onClickKeyword: (keyword: TandoorKeywordOverview) -> Unit = {},
    listContent: @Composable (pv: PaddingValues, selectedId: String?, supportsMultiplePages: Boolean, background: Color, select: (id: String?) -> Unit) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    KitshnListDetailPaneScaffold(
        modifier = modifier,
        key = key,
        topBar = topBar,
        floatingActionButton = floatingActionButton,
        listContent = listContent
    ) { selectId, supportsMultiplePanes, expandDetailPane, toggleExpandedDetailPane, close, back ->
        LaunchedEffect(selectId) {
            // hide keyboard in search layout
            focusManager.clearFocus()

            TandoorRequestState().wrapRequest {
                if(vm.tandoorClient?.container?.recipeOverview?.containsKey(selectId.toInt()) == true) return@wrapRequest
                vm.tandoorClient?.container?.recipeOverview?.put(
                    selectId.toInt(),
                    vm.tandoorClient?.recipe?.get(selectId.toInt())?.toOverview()
                )
            }
        }

        ViewRecipeDetails(
            p = ViewParameters(vm, back),

            navigationIcon = if(supportsMultiplePanes || expandDetailPane) {
                {
                    Row {
                        FilledIconButton(
                            modifier = Modifier.testTag(TestTagRepository.ACTION_CLOSE_RECIPE.name),
                            onClick = close,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                stringResource(Res.string.action_close)
                            )
                        }

                        FilledIconButton(
                            onClick = toggleExpandedDetailPane,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            when(expandDetailPane) {
                                true -> Icon(
                                    Icons.Rounded.FullscreenExit,
                                    stringResource(Res.string.action_expand_less)
                                )

                                else -> Icon(
                                    Icons.Rounded.Fullscreen,
                                    stringResource(Res.string.expand_more)
                                )
                            }
                        }
                    }
                }
            } else {
                null
            },

            recipeId = selectId.toInt(),
            client = vm.tandoorClient,

            onClickUser = { user ->
                back?.let { it() }
                onClickUser(user)
            },

            onClickKeyword = { keyword ->
                back?.let { it() }
                onClickKeyword(keyword)
            }
        )
    }
}