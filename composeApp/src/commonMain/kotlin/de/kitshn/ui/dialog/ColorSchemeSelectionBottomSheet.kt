package de.kitshn.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.materialkolor.ktx.themeColorOrNull
import de.kitshn.ui.component.ColorSchemePreviewCircle
import de.kitshn.ui.component.settings.SettingsSwitchListItem
import de.kitshn.ui.theme.custom.AvailableColorSchemes
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_okay
import kitshn.composeapp.generated.resources.settings_section_appearance_color_scheme_cupertino_label
import kitshn.composeapp.generated.resources.settings_section_appearance_color_scheme_custom_from_image_error_message
import kitshn.composeapp.generated.resources.settings_section_appearance_color_scheme_custom_from_image_error_title
import kitshn.composeapp.generated.resources.settings_section_appearance_color_scheme_custom_from_image_label
import kitshn.composeapp.generated.resources.settings_section_appearance_color_scheme_dynamic_label
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberColorSchemeSelectionBottomSheetState(): ColorSchemeSelectionBottomSheetState {
    return remember {
        ColorSchemeSelectionBottomSheetState()
    }
}

class ColorSchemeSelectionBottomSheetState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {
    fun open() {
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun ColorSchemeSelectionBottomSheet(
    state: ColorSchemeSelectionBottomSheetState,
    currentColorScheme: AvailableColorSchemes,
    onChangeCustomColorSchemeSeed: (seedColor: Color) -> Unit,
    onSelect: (colorScheme: AvailableColorSchemes) -> Unit
) {
    var render by remember { mutableStateOf(false) }
    val availableColorSchemes = remember { mutableStateListOf<AvailableColorSchemes>() }

    var showChoosePhotoBottomSheet by remember { mutableStateOf(false) }
    var showChoosePhotoErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(
        state.shown.value
    ) {
        if(state.shown.value) {
            availableColorSchemes.clear()
            availableColorSchemes.addAll(
                AvailableColorSchemes.entries
                    .filter { it.isAvailable() }
            )

            availableColorSchemes.remove(AvailableColorSchemes.ANDROID_DYNAMIC_COLOR_SCHEME)
            availableColorSchemes.remove(AvailableColorSchemes.CUPERTINO)
            availableColorSchemes.remove(AvailableColorSchemes.CUSTOM)

            render = true
        } else {
            render = false
        }
    }

    if(render) ModalBottomSheet(
        onDismissRequest = {
            state.dismiss()
        }
    ) {
        if(AvailableColorSchemes.ANDROID_DYNAMIC_COLOR_SCHEME.isAvailable()) SettingsSwitchListItem(
            label = { Text(stringResource(Res.string.settings_section_appearance_color_scheme_dynamic_label)) },
            contentDescription = stringResource(Res.string.settings_section_appearance_color_scheme_dynamic_label),
            checked = currentColorScheme == AvailableColorSchemes.ANDROID_DYNAMIC_COLOR_SCHEME,
        ) {
            onSelect(if(it) AvailableColorSchemes.ANDROID_DYNAMIC_COLOR_SCHEME else AvailableColorSchemes.DEFAULT)
        }

        if(AvailableColorSchemes.CUPERTINO.isAvailable()) SettingsSwitchListItem(
            label = { Text(stringResource(Res.string.settings_section_appearance_color_scheme_cupertino_label)) },
            contentDescription = stringResource(Res.string.settings_section_appearance_color_scheme_cupertino_label),
            checked = currentColorScheme == AvailableColorSchemes.CUPERTINO,
        ) {
            onSelect(if(it) AvailableColorSchemes.CUPERTINO else AvailableColorSchemes.DEFAULT)
        }

        LazyVerticalGrid(
            modifier = Modifier.padding(8.dp),
            columns = GridCells.FixedSize(56.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            items(availableColorSchemes.size) {
                val colorScheme = availableColorSchemes[it]

                Box(
                    Modifier
                        .padding(4.dp)
                        .size(48.dp)
                ) {
                    ColorSchemePreviewCircle(
                        colorScheme = colorScheme,
                        selected = currentColorScheme == colorScheme,
                    ) {
                        onSelect(colorScheme)
                    }
                }
            }

            item {
                Box(
                    Modifier
                        .padding(4.dp)
                        .size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp).clickable {
                            showChoosePhotoBottomSheet = true
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.PhotoLibrary,
                                stringResource(Res.string.settings_section_appearance_color_scheme_custom_from_image_label)
                            )
                        }
                    }
                }
            }
        }
    }

    ChoosePhotoBottomSheet(
        shown = showChoosePhotoBottomSheet,
        onDismiss = { showChoosePhotoBottomSheet = false },
        onSelect = {
            val color = it.decodeToImageBitmap()
                .themeColorOrNull()

            if(color == null) {
                showChoosePhotoErrorDialog = true
                return@ChoosePhotoBottomSheet
            }

            onChangeCustomColorSchemeSeed(color)
            onSelect(AvailableColorSchemes.CUSTOM)
        }
    )

    if(showChoosePhotoErrorDialog) AlertDialog(
        onDismissRequest = {
            showChoosePhotoErrorDialog = false
        },
        icon = {
            Icon(
                Icons.Rounded.ErrorOutline,
                stringResource(Res.string.settings_section_appearance_color_scheme_custom_from_image_error_title)
            )
        },
        title = {
            Text(stringResource(Res.string.settings_section_appearance_color_scheme_custom_from_image_error_title))
        },
        text = {
            Text(stringResource(Res.string.settings_section_appearance_color_scheme_custom_from_image_error_message))
        },
        confirmButton = {
            Button(
                onClick = {
                    showChoosePhotoErrorDialog = false
                }
            ) {
                Text(stringResource(Res.string.common_okay))
            }
        }
    )
}