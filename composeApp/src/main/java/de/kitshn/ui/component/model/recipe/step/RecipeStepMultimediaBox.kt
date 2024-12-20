package de.kitshn.ui.component.model.recipe.step

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import de.kitshn.R
import de.kitshn.api.tandoor.model.TandoorStep
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.ui.component.buttons.BackButton
import de.kitshn.ui.component.buttons.BackButtonType
import de.kitshn.ui.dialog.AdaptiveFullscreenDialog
import de.kitshn.ui.dialog.ImmersiveFullscreenDialog
import de.kitshn.ui.modifier.loadingPlaceHolder
import de.kitshn.ui.state.foreverRememberNotSavable
import de.kitshn.ui.state.translateState
import io.sanghun.compose.video.RepeatMode
import io.sanghun.compose.video.VideoPlayer
import io.sanghun.compose.video.controller.VideoPlayerControllerConfig
import io.sanghun.compose.video.uri.VideoPlayerMediaItem
import java.io.File

@Composable
fun RecipeStepMultimediaBox(
    contentPadding: PaddingValues = PaddingValues(),

    recipe: TandoorRecipe,
    step: TandoorStep,

    additionalContent: @Composable () -> Unit = { },
    placeholder: @Composable () -> Unit = { }
) {
    val isVideo = (step.file?.name ?: "").contains("video")

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
                .heightIn(0.dp, 400.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoDialog(
    onDismiss: () -> Unit,
    step: TandoorStep
) {
    val context = LocalContext.current

    val requestState = rememberTandoorRequestState()
    var displayFile by rememberSaveable { mutableStateOf<File?>(null) }

    LaunchedEffect(step) {
        displayFile = requestState.wrapRequest {
            step.loadFile(context)
        }
    }

    var seekTime by foreverRememberNotSavable(
        key = "RecipeStepMultimediaBox/VideoDialog/VideoPlayer/${step.file?.id}/${step.file?.name}",
        initialValue = 0L
    )

    ImmersiveFullscreenDialog(
        onDismiss = {
            onDismiss()
        },
        topBar = { },
        applyPaddingValues = false
    ) { _, _ ->
        Box(
            Modifier.fillMaxSize()
        ) {
            if(displayFile != null) {
                VideoPlayer(
                    mediaItems = listOf(
                        VideoPlayerMediaItem.StorageMediaItem(
                            storageUri = Uri.fromFile(displayFile)
                        )
                    ),
                    enablePip = false,
                    controllerConfig = VideoPlayerControllerConfig(
                        showSpeedAndPitchOverlay = true,
                        showSubtitleButton = false,
                        showCurrentTimeAndTotalTime = true,
                        showBufferingProgress = true,
                        showForwardIncrementButton = true,
                        showBackwardIncrementButton = true,
                        showBackTrackButton = false,
                        showNextTrackButton = false,
                        showRepeatModeButton = false,
                        controllerShowTimeMilliSeconds = 5_000,
                        controllerAutoShow = true,
                        showFullScreenButton = false
                    ),
                    repeatMode = RepeatMode.ONE,
                    onCurrentTimeChanged = {
                        seekTime = it
                    },
                    playerInstance = {
                        seekTo(seekTime)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                )
            } else {
                CircularProgressIndicator(
                    Modifier.align(Alignment.Center)
                )
            }

            TopAppBar(
                modifier = Modifier
                    .padding(
                        WindowInsets.safeContent.asPaddingValues()
                    )
                    .fillMaxWidth(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    BackButton(
                        onBack = onDismiss,
                        overlay = true,
                        type = BackButtonType.CLOSE
                    )
                },
                title = { },
                actions = {
                    val uriHandler = LocalUriHandler.current

                    IconButton(
                        onClick = {
                            uriHandler.openUri(step.file!!.file_download)
                        }
                    ) {
                        Icon(Icons.Rounded.Download, stringResource(R.string.action_download))
                    }
                }
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