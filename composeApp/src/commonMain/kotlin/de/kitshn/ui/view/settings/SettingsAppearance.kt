package de.kitshn.ui.view.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Loupe
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import de.kitshn.ui.component.ColorSchemePreviewCircle
import de.kitshn.ui.component.buttons.BackButton
import de.kitshn.ui.component.settings.SettingsListItem
import de.kitshn.ui.component.settings.SettingsListItemPosition
import de.kitshn.ui.component.settings.SettingsSwitchListItem
import de.kitshn.ui.dialog.ColorSchemeSelectionBottomSheet
import de.kitshn.ui.dialog.rememberColorSchemeSelectionBottomSheetState
import de.kitshn.ui.theme.custom.AvailableColorSchemes
import de.kitshn.ui.view.ViewParameters
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.settings_section_appearance_color_scheme_description
import kitshn.composeapp.generated.resources.settings_section_appearance_color_scheme_label
import kitshn.composeapp.generated.resources.settings_section_appearance_dark_mode_description
import kitshn.composeapp.generated.resources.settings_section_appearance_dark_mode_label
import kitshn.composeapp.generated.resources.settings_section_appearance_enlarge_shopping_mode_description
import kitshn.composeapp.generated.resources.settings_section_appearance_enlarge_shopping_mode_label
import kitshn.composeapp.generated.resources.settings_section_appearance_follow_system_description
import kitshn.composeapp.generated.resources.settings_section_appearance_follow_system_label
import kitshn.composeapp.generated.resources.settings_section_appearance_label
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewSettingsAppearance(
    p: ViewParameters
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(state = rememberTopAppBarState())
    val coroutineScope = rememberCoroutineScope()

    val colorSchemeName = p.vm.settings.getColorScheme.collectAsState(initial = null)
    var colorScheme by remember { mutableStateOf(AvailableColorSchemes.getDefault()) }
    LaunchedEffect(colorSchemeName.value) {
        if(colorSchemeName.value == null) return@LaunchedEffect
        AvailableColorSchemes.parse(colorSchemeName.value!!)?.let { colorScheme = it }
    }

    val customColorSchemeSeedInt =
        p.vm.settings.getCustomColorSchemeSeed.collectAsState(initial = null)
    var customColorSchemeSeed by remember { mutableStateOf(Color.Yellow) }
    LaunchedEffect(customColorSchemeSeedInt.value) {
        if(customColorSchemeSeedInt.value == null) return@LaunchedEffect
        customColorSchemeSeed = Color(customColorSchemeSeedInt.value!!)
    }

    val colorSchemeSelectionBottomSheetState = rememberColorSchemeSelectionBottomSheetState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = { BackButton(p.back) },
                title = { Text(stringResource(Res.string.settings_section_appearance_label)) },
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        val enableSystemTheme = p.vm.settings.getEnableSystemTheme.collectAsState(initial = true)

        LazyColumn(
            modifier = Modifier
                .padding(it)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            item {
                SettingsSwitchListItem(
                    position = SettingsListItemPosition.TOP,
                    label = { Text(stringResource(Res.string.settings_section_appearance_follow_system_label)) },
                    description = { Text(stringResource(Res.string.settings_section_appearance_follow_system_description)) },
                    icon = Icons.Rounded.AutoAwesome,
                    contentDescription = stringResource(Res.string.settings_section_appearance_follow_system_label),
                    checked = enableSystemTheme.value
                ) {
                    coroutineScope.launch {
                        p.vm.settings.setEnableSystemTheme(it)
                    }
                }
            }

            item {
                val checked = p.vm.settings.getEnableDarkTheme.collectAsState(initial = false)

                SettingsSwitchListItem(
                    position = SettingsListItemPosition.BOTTOM,
                    label = { Text(stringResource(Res.string.settings_section_appearance_dark_mode_label)) },
                    description = { Text(stringResource(Res.string.settings_section_appearance_dark_mode_description)) },
                    icon = Icons.Rounded.DarkMode,
                    contentDescription = stringResource(Res.string.settings_section_appearance_dark_mode_label),
                    enabled = !enableSystemTheme.value,
                    checked = checked.value
                ) {
                    coroutineScope.launch {
                        p.vm.settings.setEnableDarkTheme(it)
                    }
                }
            }

            item {
                SettingsListItem(
                    position = SettingsListItemPosition.SINGULAR,
                    label = { Text(stringResource(Res.string.settings_section_appearance_color_scheme_label)) },
                    description = { Text(stringResource(Res.string.settings_section_appearance_color_scheme_description)) },
                    icon = Icons.Rounded.Palette,
                    contentDescription = stringResource(Res.string.settings_section_appearance_color_scheme_label),
                    trailingContent = {
                        ColorSchemePreviewCircle(
                            colorScheme = colorScheme,
                            customColorSchemeSeed = customColorSchemeSeed
                        )
                    }
                ) {
                    colorSchemeSelectionBottomSheetState.open()
                }
            }

            item {
                val enlarge = p.vm.settings.getEnlargeShoppingMode.collectAsState(initial = true)

                SettingsSwitchListItem(
                    position = SettingsListItemPosition.SINGULAR,
                    label = { Text(stringResource(Res.string.settings_section_appearance_enlarge_shopping_mode_label)) },
                    description = { Text(stringResource(Res.string.settings_section_appearance_enlarge_shopping_mode_description)) },
                    icon = Icons.Rounded.Loupe,
                    contentDescription = stringResource(Res.string.settings_section_appearance_enlarge_shopping_mode_label),
                    checked = enlarge.value
                ) {
                    coroutineScope.launch {
                        p.vm.settings.setEnlargeShoppingMode(it)
                    }
                }
            }
        }
    }

    ColorSchemeSelectionBottomSheet(
        state = colorSchemeSelectionBottomSheetState,
        currentColorScheme = colorScheme,
        onChangeCustomColorSchemeSeed = {
            p.vm.settings.setCustomColorSchemeSeed(it.toArgb())
        }
    ) {
        p.vm.settings.setColorScheme(it.name)
    }
}