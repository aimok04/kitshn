package de.kitshn.ui.component.model.recipe.step

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.model.TandoorStep
import de.kitshn.api.tandoor.model.loadFile
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.ui.component.buttons.BackButton
import de.kitshn.ui.component.buttons.BackButtonType
import de.kitshn.ui.dialog.ImmersiveFullscreenDialog
import de.kitshn.ui.state.foreverRememberNotSavable
import io.sanghun.compose.video.RepeatMode
import io.sanghun.compose.video.VideoPlayer
import io.sanghun.compose.video.controller.VideoPlayerControllerConfig
import io.sanghun.compose.video.uri.VideoPlayerMediaItem
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_download
import org.jetbrains.compose.resources.stringResource
import java.io.File

@Composable
actual fun isVideoSupported(): Boolean {
    return true
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun VideoDialog(
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
                ContainedLoadingIndicator(
                    Modifier
                        .align(Alignment.Center)
                        .size(96.dp)
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
                        Icon(Icons.Rounded.Download, stringResource(Res.string.action_download))
                    }
                }
            )
        }
    }
}