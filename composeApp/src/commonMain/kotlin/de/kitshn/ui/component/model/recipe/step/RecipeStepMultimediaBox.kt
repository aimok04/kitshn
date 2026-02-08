package de.kitshn.ui.component.model.recipe.step

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
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import de.kitshn.api.tandoor.model.TandoorStep
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.ui.dialog.AdaptiveFullscreenDialog
import de.kitshn.ui.modifier.loadingPlaceHolder
import de.kitshn.ui.state.translateState
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_download
import org.jetbrains.compose.resources.stringResource

@Composable
fun RecipeStepMultimediaBox(
    contentPadding: PaddingValues = PaddingValues(),

    recipe: TandoorRecipe,
    step: TandoorStep,

    additionalContent: @Composable () -> Unit = { },
    placeholder: @Composable () -> Unit = { }
) {
    val isVideo = (step.file?.name ?: "").contains("video")

    if(isVideo && !isVideoSupported()) {
        Text(
            modifier = Modifier.padding(contentPadding)
                .padding(8.dp)
                .fillMaxWidth(),
            text = "Video playback is not supported on this platform.",
            textAlign = TextAlign.Center
        )

        return
    }

    if((step.file?.preview ?: "").isBlank() && !isVideo) {
        placeholder()
        return
    }

    var showDialog by rememberSaveable { mutableStateOf(false) }

    Box {
        var imageLoadingState by remember {
            mutableStateOf<AsyncImagePainter.State>(
                AsyncImagePainter.State.Loading(
                    null
                )
            )
        }

        AsyncImage(
            model = if(isVideo) {
                recipe.loadThumbnail()
            } else {
                step.loadFilePreview()
            },
            onState = {
                imageLoadingState = it
            },
            contentDescription = step.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth()
                .heightIn(200.dp, 400.dp)
                .loadingPlaceHolder(
                    loadingState = imageLoadingState.translateState(),
                    shape = RectangleShape
                )
                .clickable {
                    showDialog = true
                }
                .run {
                    if(isVideo) {
                        blur(64.dp)
                    } else {
                        this
                    }
                }
        )

        additionalContent()

        if(isVideo) LargeFloatingActionButton(
            modifier = Modifier.align(Alignment.Center),
            onClick = { showDialog = true },
            elevation = FloatingActionButtonDefaults.elevation(
                0.dp, 0.dp, 0.dp, 0.dp
            )
        ) {
            Icon(
                Icons.Rounded.PlayArrow,
                "play"
            )
        }
    }

    if(showDialog) {
        if(isVideo) {
            VideoDialog(
                onDismiss = {
                    showDialog = false
                },
                step = step
            )
        } else {
            ImageDialog(
                onDismiss = {
                    showDialog = false
                },
                step = step
            )
        }
    }
}

@Composable
private fun ImageDialog(
    onDismiss: () -> Unit,
    step: TandoorStep
) {
    var imageLoadingState by remember {
        mutableStateOf<AsyncImagePainter.State>(
            AsyncImagePainter.State.Loading(
                null
            )
        )
    }

    AdaptiveFullscreenDialog(
        onDismiss = {
            onDismiss()
        },
        title = {
            Text(step.file!!.name)
        },
        topAppBarActions = {
            val uriHandler = LocalUriHandler.current

            IconButton(
                onClick = {
                    uriHandler.openUri(step.file!!.file_download)
                }
            ) {
                Icon(Icons.Rounded.Download, stringResource(Res.string.action_download))
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

@Composable
expect fun isVideoSupported(): Boolean

@Composable
expect fun VideoDialog(
    onDismiss: () -> Unit,
    step: TandoorStep
)