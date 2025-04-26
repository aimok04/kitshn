package de.kitshn.ui.dialog.peekaboo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import com.preat.peekaboo.ui.camera.PeekabooCamera
import com.preat.peekaboo.ui.camera.rememberPeekabooCameraState
import de.kitshn.ui.component.alert.FullSizeAlertPane
import de.kitshn.ui.dialog.AdaptiveFullscreenDialog
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_reset
import kitshn.composeapp.generated.resources.action_switch_camera
import kitshn.composeapp.generated.resources.action_take_photo
import kitshn.composeapp.generated.resources.common_select
import kitshn.composeapp.generated.resources.permissions_camera_alert
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun photoTakingDialogImpl(
    shown: Boolean,
    onSelect: (uri: ByteArray) -> Unit,
    onDismiss: () -> Unit
): Boolean {
    if(!shown) return true

    val context = LocalPlatformContext.current
    val imageLoader = remember { ImageLoader(context) }

    var capturedImage by remember { mutableStateOf<ByteArray?>(null) }

    val state = rememberPeekabooCameraState(
        onCapture = {
            capturedImage = it
        }
    )

    AdaptiveFullscreenDialog(
        onDismiss = {
            onDismiss()
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(
                        onClick = {
                            if(capturedImage != null) {
                                capturedImage = null
                            } else {
                                state.toggleCamera()
                            }
                        }
                    ) {
                        if(capturedImage != null) {
                            Icon(Icons.Rounded.Replay, stringResource(Res.string.action_reset))
                        } else {
                            Icon(
                                Icons.Rounded.Cameraswitch,
                                stringResource(Res.string.action_switch_camera)
                            )
                        }
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                        onClick = {
                            if(capturedImage != null) {
                                onSelect(capturedImage!!)
                                onDismiss()
                            } else {
                                state.capture()
                            }
                        }
                    ) {
                        if(capturedImage != null) {
                            Icon(Icons.Rounded.Check, stringResource(Res.string.common_select))
                        } else {
                            Icon(Icons.Rounded.Camera, stringResource(Res.string.action_take_photo))
                        }
                    }
                }
            )
        },
        forceFullscreen = true,
        disableAnimation = true
    ) { _, _, _ ->
        Box(
            Modifier.fillMaxSize()
        ) {
            if(capturedImage != null) {
                AsyncImage(
                    model = capturedImage,
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    imageLoader = imageLoader,
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .fillMaxSize()
                )
            } else {
                PeekabooCamera(
                    state = state,
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .fillMaxSize(),
                    permissionDeniedContent = {
                        FullSizeAlertPane(
                            imageVector = Icons.Rounded.Shield,
                            text = stringResource(Res.string.permissions_camera_alert),
                            contentDescription = stringResource(Res.string.permissions_camera_alert)
                        )
                    }
                )
            }
        }
    }

    return true
}