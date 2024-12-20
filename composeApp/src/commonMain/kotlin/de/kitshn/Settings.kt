package de.kitshn

import androidx.lifecycle.ViewModel
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.observable.makeObservable
import de.kitshn.api.tandoor.TandoorCredentials
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString

const val KEY_SETTINGS_APPEARANCE_SYSTEM_THEME = "appearance_system_theme"
const val KEY_SETTINGS_APPEARANCE_DARK_THEME = "appearance_dark_theme"
const val KEY_SETTINGS_APPEARANCE_DYNAMIC_COLORS = "appearance_dynamic_colors"

const val KEY_SETTINGS_BEHAVIOR_USE_SHARE_WRAPPER = "behavior_use_share_wrapper"
const val KEY_SETTINGS_BEHAVIOR_USE_SHARE_WRAPPER_HINT_SHOWN = "behavior_use_share_wrapper_hint_shown_str"
const val KEY_SETTINGS_BEHAVIOR_HIDE_INGREDIENT_ALLOCATION_ACTION_CHIPS = "behavior_hide_ingredient_allocation_action_chips"
const val KEY_SETTINGS_BEHAVIOR_INGREDIENTS_SHOW_FRACTIONAL_VALUES = "behavior_ingredients_show_fractional_values"
const val KEY_SETTINGS_BEHAVIOR_PROPERTIES_SHOW_FRACTIONAL_VALUES = "behavior_properties_show_fractional_values"

const val KEY_SETTINGS_ONBOARDING_COMPLETED = "onboarding_completed"
const val KEY_SETTINGS_TANDOOR_CREDENTIALS = "tandoor_credentials"

const val KEY_SETTINGS_LATEST_VERSION_CHECK = "latest_version_check"

@OptIn(ExperimentalSettingsApi::class)
class SettingsViewModel : ViewModel() {

    val settings: Settings = Settings()
    private val obs: ObservableSettings = settings.makeObservable()

    // latest version check
    val getLatestVersionCheck: Flow<String> = obs.getStringFlow(KEY_SETTINGS_LATEST_VERSION_CHECK, "")
    fun setLatestVersionCheck(version: String) = obs.putString(KEY_SETTINGS_LATEST_VERSION_CHECK, version)

    // onboarding
    val getOnboardingCompleted: Flow<Boolean> = obs.getBooleanFlow(KEY_SETTINGS_ONBOARDING_COMPLETED, false)
    fun setOnboardingCompleted(done: Boolean) = obs.putBoolean(KEY_SETTINGS_ONBOARDING_COMPLETED, done)

    // credentials
    val getTandoorCredentials: Flow<TandoorCredentials?> = obs.getStringFlow(
        KEY_SETTINGS_TANDOOR_CREDENTIALS, "{}").map { json.maybeDecodeFromString<TandoorCredentials>(it) }

    fun saveTandoorCredentials(credentials: TandoorCredentials?) = obs.putString(KEY_SETTINGS_TANDOOR_CREDENTIALS, json.encodeToString(credentials))

    // design
    val getEnableDynamicColors: Flow<Boolean> = obs.getBooleanFlow(KEY_SETTINGS_APPEARANCE_DYNAMIC_COLORS, true)
    val getEnableSystemTheme: Flow<Boolean> = obs.getBooleanFlow(KEY_SETTINGS_APPEARANCE_SYSTEM_THEME, true)
    val getEnableDarkTheme: Flow<Boolean> = obs.getBooleanFlow(KEY_SETTINGS_APPEARANCE_DARK_THEME, false)

    fun setEnableDynamicColors(enable: Boolean) = obs.putBoolean(KEY_SETTINGS_APPEARANCE_DYNAMIC_COLORS, enable)
    fun setEnableSystemTheme(enable: Boolean) = obs.putBoolean(KEY_SETTINGS_APPEARANCE_SYSTEM_THEME, enable)
    fun setEnableDarkTheme(enable: Boolean) =  obs.putBoolean(KEY_SETTINGS_APPEARANCE_DARK_THEME, enable)

    //behavior
    val getUseShareWrapper: Flow<Boolean> = obs.getBooleanFlow(KEY_SETTINGS_BEHAVIOR_USE_SHARE_WRAPPER, true)
    fun setUseShareWrapper(use: Boolean) = obs.putBoolean(KEY_SETTINGS_BEHAVIOR_USE_SHARE_WRAPPER, use)

    val getUseShareWrapperHintShown: Flow<String> = obs.getStringFlow(KEY_SETTINGS_BEHAVIOR_USE_SHARE_WRAPPER_HINT_SHOWN, "")
    fun setUseShareWrapperHintShown(url: String) = obs.putString(KEY_SETTINGS_BEHAVIOR_USE_SHARE_WRAPPER_HINT_SHOWN, url)

    val getHideIngredientAllocationActionChips: Flow<Boolean> = obs.getBooleanFlow(KEY_SETTINGS_BEHAVIOR_HIDE_INGREDIENT_ALLOCATION_ACTION_CHIPS, false)
    fun setHideIngredientAllocationActionChips(hide: Boolean) = obs.putBoolean(KEY_SETTINGS_BEHAVIOR_HIDE_INGREDIENT_ALLOCATION_ACTION_CHIPS, hide)

    val getIngredientsShowFractionalValues: Flow<Boolean> = obs.getBooleanFlow(KEY_SETTINGS_BEHAVIOR_INGREDIENTS_SHOW_FRACTIONAL_VALUES, true)
    fun setIngredientsShowFractionalValues(show: Boolean) = obs.putBoolean(KEY_SETTINGS_BEHAVIOR_INGREDIENTS_SHOW_FRACTIONAL_VALUES, show)

    val getPropertiesShowFractionalValues: Flow<Boolean> = obs.getBooleanFlow(KEY_SETTINGS_BEHAVIOR_PROPERTIES_SHOW_FRACTIONAL_VALUES, true)
    fun setPropertiesShowFractionalValues(show: Boolean) = obs.putBoolean(KEY_SETTINGS_BEHAVIOR_PROPERTIES_SHOW_FRACTIONAL_VALUES, show)

}