package de.kitshn.model.form.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import de.kitshn.ui.dialog.ChoosePhotoBottomSheet
import de.kitshn.ui.modifier.loadingPlaceHolder
import de.kitshn.ui.state.translateState
import de.kitshn.ui.theme.Typography
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_click_to_upload_image
import kitshn.composeapp.generated.resources.action_reset
import kitshn.composeapp.generated.resources.action_upload
import org.jetbrains.compose.resources.stringResource

class KitshnFormImageUploadItem(
    val currentImage: @Composable () -> ImageRequest?,

    val value: () -> ByteArray?,
    val onValueChange: (image: ByteArray?) -> Unit,

    val label: @Composable () -> String,
) : KitshnFormBaseItem() {

    @Composable
    override fun Render(
        modifier: Modifier
    ) {
        val context = LocalPlatformContext.current
        val imageLoader = remember { ImageLoader(context) }

        var imageLoadingState by remember {
            mutableStateOf<AsyncImagePainter.State>(
                AsyncImagePainter.State.Loading(null)
            )
        }

        var showChoosePhotoBottomSheet by remember { mutableStateOf(false) }
        ChoosePhotoBottomSheet(
            shown = showChoosePhotoBottomSheet,
            onSelect = {
                onValueChange(it)
            }
        ) { showChoosePhotoBottomSheet = false }

        Box {
            OutlinedCard(
                modifier
                    .height(170.dp)
                    .clickable {
                        showChoosePhotoBottomSheet = true
                    }
            ) {
                if(currentImage() != null || value() != null) {
                    if(value() != null) {
                        AsyncImage(
                            model = value(),
                            onState = {
                                imageLoadingState = it
                            },
                            contentDescription = label(),
                            contentScale = ContentScale.Crop,
                            imageLoader = imageLoader,
                            modifier = Modifier
                                .fillMaxSize()
                                .loadingPlaceHolder(imageLoadingState.translateState())
                        )
                    } else {
                        AsyncImage(
                            model = currentImage(),
                            onState = {
                                imageLoadingState = it
                            },
                            contentDescription = label(),
                            contentScale = ContentScale.Crop,
                            imageLoader = imageLoader,
                            modifier = Modifier
                                .fillMaxSize()
                                .loadingPlaceHolder(imageLoadingState.translateState())
                        )
                    }
                } else {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier
                                    .width(64.dp)
                                    .height(64.dp),
                                imageVector = Icons.Rounded.Image,
                                contentDescription = label(),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(
                                Modifier.padding(start = 8.dp, end = 8.dp)
                            )

                            Column {
                                Text(
                                    text = label(),
                                    style = Typography().titleMedium
                                )

                                Text(
                                    text = stringResource(Res.string.action_click_to_upload_image),
                                    style = Typography().labelLarge
                                )
                            }
                        }
                    }
                }
            }

            if(currentImage() != null || value() != null) Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 8.dp)
            ) {
                if(value() != null) Box(
                    Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SmallFloatingActionButton(
                        onClick = { onValueChange(null) },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Icon(Icons.Rounded.Restore, stringResource(Res.string.action_reset))
                    }
                }

                FloatingActionButton(
                    onClick = {
                        showChoosePhotoBottomSheet = true
                    }
                ) {
                    Icon(Icons.Rounded.Upload, stringResource(Res.string.action_upload))
                }
            }
        }
    }

    override suspend fun submit(): Boolean {
        return true
    }

}