package de.kitshn.android.ui.component.model.recipe.step

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.model.TandoorStep
import de.kitshn.android.ui.dialog.AdaptiveFullscreenDialog
import de.kitshn.android.ui.modifier.loadingPlaceHolder
import de.kitshn.android.ui.state.translateState

@Composable
fun RecipeStepMultimediaBox(
    contentPadding: PaddingValues = PaddingValues(),
    step: TandoorStep,

    additionalContent: @Composable () -> Unit = { },
    placeholder: @Composable () -> Unit = { }
) {
    if((step.file?.preview ?: "").isBlank()) {
        placeholder()
        return
    }

    val uriHandler = LocalUriHandler.current

    var showDialog by remember { mutableStateOf(false) }

    var imageLoadingState by remember {
        mutableStateOf<AsyncImagePainter.State>(
            AsyncImagePainter.State.Loading(
                null
            )
        )
    }

    Box {
        AsyncImage(
            model = step.loadFilePreview(),
            onState = {
                imageLoadingState = it
            },
            contentDescription = step.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth()
                .heightIn(0.dp, 400.dp)
                .loadingPlaceHolder(
                    loadingState = imageLoadingState.translateState(),
                    shape = RectangleShape
                )
                .clickable {
                    showDialog = true
                }
        )

        additionalContent()
    }

    if(showDialog) AdaptiveFullscreenDialog(
        onDismiss = {
            showDialog = false
        },
        title = {
            Text(step.file!!.name)
        },
        topAppBarActions = {
            IconButton(
                onClick = {
                    uriHandler.openUri(step.file!!.file_download)
                }
            ) {
                Icon(Icons.Rounded.Download, stringResource(R.string.action_download))
            }
        }
    ) { _, _, _ ->
        AsyncImage(
            model = step.loadFilePreview(),
            onState = {
                imageLoadingState = it
            },
            contentDescription = step.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .loadingPlaceHolder(
                    loadingState = imageLoadingState.translateState(),
                    shape = RectangleShape
                )
                .fillMaxSize()
                .background(Color.Black)
        )
    }
}