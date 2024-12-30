package de.kitshn.migration

import de.kitshn.SettingsViewModel
import kotlinx.coroutines.flow.first

/**
 * Migrate Android data store settings to multiplatform settings / shared preferences
 *
 * @return true if successful
 */
suspend fun runSettingsMigration(
    settings: SettingsViewModel,
    legacySettings: LegacySettingsViewModel
): Boolean {
    settings.setLatestVersionCheck(
        legacySettings.getLatestVersionCheck.first()
    )

    settings.setOnboardingCompleted(
        legacySettings.getOnboardingCompleted.first()
    )

    settings.saveTandoorCredentials(
        legacySettings.getTandoorCredentials.first()
    )

    settings.setEnableDynamicColors(
        legacySettings.getEnableDynamicColors.first()
    )

    settings.setEnableSystemTheme(
        legacySettings.getEnableSystemTheme.first()
    )

    settings.setEnableDarkTheme(
        legacySettings.getEnableDarkTheme.first()
    )

    settings.setUseShareWrapper(
        legacySettings.getUseShareWrapper.first()
    )

    settings.setUseShareWrapperHintShown(
        legacySettings.getUseShareWrapperHintShown.first()
    )

    settings.setHideIngredientAllocationActionChips(
        legacySettings.getHideIngredientAllocationActionChips.first()
    )

    settings.setIngredientsShowFractionalValues(
        legacySettings.getIngredientsShowFractionalValues.first()
    )

    settings.setPropertiesShowFractionalValues(
        legacySettings.getPropertiesShowFractionalValues.first()
    )

    return true
}