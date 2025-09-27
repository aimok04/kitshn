package de.kitshn.ui.dialog


import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Photo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import de.kitshn.ui.component.settings.SettingsListItem
import de.kitshn.ui.component.settings.SettingsListItemPosition
import io.github.vinceglb.filekit.dialogs.FileKitCameraType
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_choose_existing_photo
import kitshn.composeapp.generated.resources.action_take_new_photo
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun ChoosePhotoBottomSheet(
    shown: Boolean,
    onSelect: (uri: ByteArray) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState()

    val photoPickerLauncher = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        onDismiss()

        coroutineScope.launch {
            file?.readBytes()?.let { onSelect(it) }
        }
    }

    val cameraPickerLauncher =
        rememberCameraPickerLauncher(type = FileKitCameraType.Photo) { file ->
            onDismiss()

            coroutineScope.launch {
                file?.readBytes()?.let { onSelect(it) }
            }
        }

    if(shown) ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            onDismiss()
        }
    ) {
        Column {
            SettingsListItem(
                position = SettingsListItemPosition.TOP,
                icon = Icons.Rounded.CameraAlt,
                label = { Text(stringResource(Res.string.action_take_new_photo)) },
                contentDescription = stringResource(Res.string.action_take_new_photo)
            ) {
                cameraPickerLauncher.launch()
            }

            SettingsListItem(
                position = SettingsListItemPosition.BOTTOM,
                icon = Icons.Rounded.Photo,
                label = { Text(stringResource(Res.string.action_choose_existing_photo)) },
                contentDescription = stringResource(Res.string.action_choose_existing_photo)
            ) {
                photoPickerLauncher.launch()
            }
        }
    }
}