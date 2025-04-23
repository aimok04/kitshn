package de.kitshn

import androidx.lifecycle.ViewModel
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.coroutines.getIntOrNullFlow
import com.russhwolf.settings.coroutines.getLongFlow
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import com.russhwolf.settings.observable.makeObservable
import de.kitshn.api.tandoor.TandoorCredentials
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

const val KEY_SETTINGS_APPEARANCE_SYSTEM_THEME = "appearance_system_theme"
const val KEY_SETTINGS_APPEARANCE_DARK_THEME = "appearance_dark_theme"
const val KEY_SETTINGS_APPEARANCE_COLOR_SCHEME = "appearance_color_scheme"
const val KEY_SETTINGS_APPEARANCE_CUSTOM_COLOR_SCHEME_SEED = "appearance_custom_color_scheme_seed"
const val KEY_SETTINGS_APPEARANCE_ENLARGE_SHOPPING_MODE = "appearance_enlarge_shopping_mode"

const val KEY_SETTINGS_BEHAVIOR_USE_SHARE_WRAPPER = "behavior_use_share_wrapper"
const val KEY_SETTINGS_BEHAVIOR_USE_SHARE_WRAPPER_HINT_SHOWN =
    "behavior_use_share_wrapper_hint_shown_str"
const val KEY_SETTINGS_BEHAVIOR_HIDE_INGREDIENT_ALLOCATION_ACTION_CHIPS =
    "behavior_hide_ingredient_allocation_action_chips"
const val KEY_SETTINGS_BEHAVIOR_ENABLE_MEAL_PLAN_PROMOTION =
    "behavior_enable_meal_plan_promotion"
const val KEY_SETTINGS_BEHAVIOR_PROMOTE_TOMORROWS_MEAL_PLAN =
    "behavior_promote_tomorrows_meal_plan"
const val KEY_SETTINGS_BEHAVIOR_ENABLE_DYNAMIC_HOME_SCREEN =
    "behavior_enable_dynamic_home_screen"
const val KEY_SETTINGS_BEHAVIOR_INGREDIENTS_SHOW_FRACTIONAL_VALUES =
    "behavior_ingredients_show_fractional_values"
const val KEY_SETTINGS_BEHAVIOR_PROPERTIES_SHOW_FRACTIONAL_VALUES =
    "behavior_properties_show_fractional_values"
const val KEY_SETTINGS_BEHAVIOR_HIDE_FUNDING_BANNER_UNTIL =
    "behavior_hide_funding_banner_until"

const val KEY_SETTINGS_ONBOARDING_COMPLETED = "onboarding_completed"
const val KEY_SETTINGS_TANDOOR_CREDENTIALS = "tandoor_credentials"

const val KEY_SETTINGS_LATEST_VERSION_CHECK = "latest_version_check"

const val KEY_SETTINGS_FIRST_RUN_TIME = "first_run_time"

@OptIn(ExperimentalSettingsApi::class)
class SettingsViewModel : ViewModel() {

    val settings: Settings = Settings()
    private val obs: ObservableSettings = settings.makeObservable()

    // first run time (since unix epoch in seconds)
    val getFirstRunTime: Flow<Long> =
        obs.getLongFlow(KEY_SETTINGS_FIRST_RUN_TIME, -1L)

    fun setFirstRunTime() =
        obs.putLong(KEY_SETTINGS_FIRST_RUN_TIME, Clock.System.now().epochSeconds)

    // latest version check
    val getLatestVersionCheck: Flow<String> =
        obs.getStringFlow(KEY_SETTINGS_LATEST_VERSION_CHECK, "")

    fun setLatestVersionCheck(version: String) =
        obs.putString(KEY_SETTINGS_LATEST_VERSION_CHECK, version)

    // onboarding
    val getOnboardingCompleted: Flow<Boolean> =
        obs.getBooleanFlow(KEY_SETTINGS_ONBOARDING_COMPLETED, false)

    fun setOnboardingCompleted(done: Boolean) =
        obs.putBoolean(KEY_SETTINGS_ONBOARDING_COMPLETED, done)

    // credentials
    val getTandoorCredentials: Flow<TandoorCredentials?> = obs.getStringFlow(
        KEY_SETTINGS_TANDOOR_CREDENTIALS, "{}"
    ).map { json.maybeDecodeFromString<TandoorCredentials>(it) }

    fun saveTandoorCredentials(credentials: TandoorCredentials?) =
        obs.putString(KEY_SETTINGS_TANDOOR_CREDENTIALS, json.encodeToString(credentials))

    // design
    val getColorScheme: Flow<String?> =
        obs.getStringOrNullFlow(KEY_SETTINGS_APPEARANCE_COLOR_SCHEME)

    val getCustomColorSchemeSeed: Flow<Int?> =
        obs.getIntOrNullFlow(KEY_SETTINGS_APPEARANCE_CUSTOM_COLOR_SCHEME_SEED)

    val getEnableSystemTheme: Flow<Boolean> =
        obs.getBooleanFlow(KEY_SETTINGS_APPEARANCE_SYSTEM_THEME, true)

    val getEnableDarkTheme: Flow<Boolean> =
        obs.getBooleanFlow(KEY_SETTINGS_APPEARANCE_DARK_THEME, false)

    fun setColorScheme(name: String) =
        obs.putString(KEY_SETTINGS_APPEARANCE_COLOR_SCHEME, name)

    fun setCustomColorSchemeSeed(seedColor: Int) =
        obs.putInt(KEY_SETTINGS_APPEARANCE_CUSTOM_COLOR_SCHEME_SEED, seedColor)

    fun setEnableSystemTheme(enable: Boolean) =
        obs.putBoolean(KEY_SETTINGS_APPEARANCE_SYSTEM_THEME, enable)

    fun setEnableDarkTheme(enable: Boolean) =
        obs.putBoolean(KEY_SETTINGS_APPEARANCE_DARK_THEME, enable)

    val getEnlargeShoppingMode: Flow<Boolean> =
        obs.getBooleanFlow(KEY_SETTINGS_APPEARANCE_ENLARGE_SHOPPING_MODE, true)

    fun setEnlargeShoppingMode(enlarge: Boolean) =
        obs.putBoolean(KEY_SETTINGS_APPEARANCE_ENLARGE_SHOPPING_MODE, enlarge)

    // behavior
    val getUseShareWrapper: Flow<Boolean> =
        obs.getBooleanFlow(KEY_SETTINGS_BEHAVIOR_USE_SHARE_WRAPPER, true)

    fun setUseShareWrapper(use: Boolean) =
        obs.putBoolean(KEY_SETTINGS_BEHAVIOR_USE_SHARE_WRAPPER, use)

    val getUseShareWrapperHintShown: Flow<String> =
        obs.getStringFlow(KEY_SETTINGS_BEHAVIOR_USE_SHARE_WRAPPER_HINT_SHOWN, "")

    fun setUseShareWrapperHintShown(url: String) =
        obs.putString(KEY_SETTINGS_BEHAVIOR_USE_SHARE_WRAPPER_HINT_SHOWN, url)

    val getHideIngredientAllocationActionChips: Flow<Boolean> =
        obs.getBooleanFlow(KEY_SETTINGS_BEHAVIOR_HIDE_INGREDIENT_ALLOCATION_ACTION_CHIPS, false)

    fun setHideIngredientAllocationActionChips(hide: Boolean) =
        obs.putBoolean(KEY_SETTINGS_BEHAVIOR_HIDE_INGREDIENT_ALLOCATION_ACTION_CHIPS, hide)

    val getEnableMealPlanPromotion: Flow<Boolean> =
        obs.getBooleanFlow(KEY_SETTINGS_BEHAVIOR_ENABLE_MEAL_PLAN_PROMOTION, true)

    fun setEnableMealPlanPromotion(enable: Boolean) =
        obs.putBoolean(KEY_SETTINGS_BEHAVIOR_ENABLE_MEAL_PLAN_PROMOTION, enable)

    val getPromoteTomorrowsMealPlan: Flow<Boolean> =
        obs.getBooleanFlow(KEY_SETTINGS_BEHAVIOR_PROMOTE_TOMORROWS_MEAL_PLAN, false)

    fun setPromoteTomorrowsMealPlan(promote: Boolean) =
        obs.putBoolean(KEY_SETTINGS_BEHAVIOR_PROMOTE_TOMORROWS_MEAL_PLAN, promote)

    val getEnableDynamicHomeScreen: Flow<Boolean> =
        obs.getBooleanFlow(KEY_SETTINGS_BEHAVIOR_ENABLE_DYNAMIC_HOME_SCREEN, true)

    fun setEnableDynamicHomeScreen(enable: Boolean) =
        obs.putBoolean(KEY_SETTINGS_BEHAVIOR_ENABLE_DYNAMIC_HOME_SCREEN, enable)

    val getIngredientsShowFractionalValues: Flow<Boolean> =
        obs.getBooleanFlow(KEY_SETTINGS_BEHAVIOR_INGREDIENTS_SHOW_FRACTIONAL_VALUES, true)

    fun setIngredientsShowFractionalValues(show: Boolean) =
        obs.putBoolean(KEY_SETTINGS_BEHAVIOR_INGREDIENTS_SHOW_FRACTIONAL_VALUES, show)

    val getPropertiesShowFractionalValues: Flow<Boolean> =
        obs.getBooleanFlow(KEY_SETTINGS_BEHAVIOR_PROPERTIES_SHOW_FRACTIONAL_VALUES, true)

    fun setPropertiesShowFractionalValues(show: Boolean) =
        obs.putBoolean(KEY_SETTINGS_BEHAVIOR_PROPERTIES_SHOW_FRACTIONAL_VALUES, show)

    val getFundingBannerHideUntil: Flow<Long> =
        obs.getLongFlow(KEY_SETTINGS_BEHAVIOR_HIDE_FUNDING_BANNER_UNTIL, -1L)

    fun setFundingBannerHideUntil(epochSeconds: Long) =
        obs.putLong(KEY_SETTINGS_BEHAVIOR_HIDE_FUNDING_BANNER_UNTIL, epochSeconds)

}