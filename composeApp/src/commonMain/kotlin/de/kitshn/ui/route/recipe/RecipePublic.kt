package de.kitshn.ui.route.recipe

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.closeAppHandler
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.alert.FullSizeAlertPane
import de.kitshn.ui.component.buttons.BackButton
import de.kitshn.ui.component.buttons.BackButtonType
import de.kitshn.ui.component.settings.SettingsListItem
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.view.ViewParameters
import de.kitshn.ui.view.recipe.details.ViewRecipeDetails
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_shared_recipe
import kitshn.composeapp.generated.resources.common_source
import kitshn.composeapp.generated.resources.recipe_not_found
import org.jetbrains.compose.resources.stringResource

@Composable
fun RouteRecipePublic(
    p: RouteParameters
) {
    val closeAppHandler = closeAppHandler()

    val recipeId = p.bse.arguments?.getString("recipeId")
    val shareToken = p.bse.arguments?.getString("shareToken")

    if(recipeId == null || shareToken == null) {
        FullSizeAlertPane(
            imageVector = Icons.Rounded.SearchOff,
            contentDescription = stringResource(Res.string.recipe_not_found),
            text = stringResource(Res.string.recipe_not_found)
        )

        return
    }

    val client = p.vm.uiState.shareClient ?: return

    // retrieve shared recipe
    val requestState = rememberTandoorRequestState()
    LaunchedEffect(recipeId, shareToken) {
        requestState.wrapRequest {
            val recipe = client.recipe.get(id = recipeId.toInt(), share = shareToken)

            // change id to avoid conflicts
            recipe.id = -recipe.id

            client.container.recipe[recipe.id] = recipe
            client.container.recipeOverview[recipe.id] = recipe.toOverview()

            if(p.vm.tandoorClient == null) {
                p.vm.tandoorClient = client
            } else {
                p.vm.tandoorClient!!.container.recipe[recipe.id] = recipe
                p.vm.tandoorClient!!.container.recipeOverview[recipe.id] = recipe.toOverview()
            }
        }
    }

    ViewRecipeDetails(
        p = ViewParameters(p.vm, p.onBack),

        navigationIcon = {
            BackButton(
                onBack = {
                    closeAppHandler()
                },
                overlay = true,
                type = BackButtonType.CLOSE
            )
        },
        prependContent = {
            SettingsListItem(
                icon = Icons.Rounded.Share,
                label = { Text(text = stringResource(Res.string.common_shared_recipe)) },
                description = { Text(text = "${stringResource(Res.string.common_source)}: ${client.credentials.instanceUrl}") },
                contentDescription = stringResource(Res.string.common_shared_recipe),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                ),
            )
        },

        recipeId = -(recipeId.toInt()),
        client = client,
        shareToken = shareToken,

        onClickKeyword = { }
    )

    TandoorRequestErrorHandler(requestState)
}