package de.kitshn.ui.dialog.external

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.os.BuildCompat
import com.google.modernstorage.photopicker.PhotoPicker
import kotlinx.coroutines.launch

@OptIn(BuildCompat.PrereleaseSdkCheck::class)
@Composable
fun PhotoPickerDialog(
    shown: Boolean,
    onSelect: (uri: Uri) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val profileAvatarLauncher = rememberLauncherForActivityResult(PhotoPicker()) { uris ->
        onDismiss()

        coroutineScope.launch {
            if(uris.isEmpty()) return@launch
            onSelect(uris[0])
        }
    }

    LaunchedEffect(shown) {
        if(!shown) return@LaunchedEffect
        profileAvatarLauncher.launch(PhotoPicker.Args(PhotoPicker.Type.IMAGES_ONLY, 1))
    }
}