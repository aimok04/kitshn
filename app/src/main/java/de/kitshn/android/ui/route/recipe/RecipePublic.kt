package de.kitshn.android.ui.route.recipe

import android.app.Activity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.rememberTandoorRequestState
import de.kitshn.android.ui.TandoorRequestErrorHandler
import de.kitshn.android.ui.component.alert.FullSizeAlertPane
import de.kitshn.android.ui.component.buttons.BackButton
import de.kitshn.android.ui.component.buttons.BackButtonType
import de.kitshn.android.ui.component.settings.SettingsListItem
import de.kitshn.android.ui.route.RouteParameters
import de.kitshn.android.ui.view.ViewParameters
import de.kitshn.android.ui.view.recipe.details.ViewRecipeDetails

@Composable
fun RouteRecipePublic(
    p: RouteParameters
) {
    val context = LocalContext.current

    val recipeId = p.bse.arguments?.getString("recipeId")
    val shareToken = p.bse.arguments?.getString("shareToken")

    if(recipeId == null || shareToken == null) {
        FullSizeAlertPane(
            imageVector = Icons.Rounded.SearchOff,
            contentDescription = stringResource(R.string.recipe_not_found),
            text = stringResource(R.string.recipe_not_found)
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
                    (context as? Activity)?.finish()
                },
                overlay = true,
                type = BackButtonType.CLOSE
            )
        },
        prependContent = {
            SettingsListItem(
                icon = Icons.Rounded.Share,
                label = { Text(text = "Geteiltes Rezept") },
                description = { Text(text = "Quelle: ${client.credentials.instanceUrl}") },
                contentDescription = "Geteiltes Rezept",
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