package de.kitshn.migration

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import de.kitshn.api.tandoor.TandoorCredentials
import de.kitshn.json
import de.kitshn.maybeDecodeFromString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val KEY_SETTINGS_APPEARANCE_SYSTEM_THEME = booleanPreferencesKey("appearance_system_theme")
val KEY_SETTINGS_APPEARANCE_DARK_THEME = booleanPreferencesKey("appearance_dark_theme")
val KEY_SETTINGS_APPEARANCE_DYNAMIC_COLORS = booleanPreferencesKey("appearance_dynamic_colors")

val KEY_SETTINGS_BEHAVIOR_USE_SHARE_WRAPPER =
    booleanPreferencesKey("behavior_use_share_wrapper")
val KEY_SETTINGS_BEHAVIOR_USE_SHARE_WRAPPER_HINT_SHOWN =
    stringPreferencesKey("behavior_use_share_wrapper_hint_shown_str")
val KEY_SETTINGS_BEHAVIOR_HIDE_INGREDIENT_ALLOCATION_ACTION_CHIPS =
    booleanPreferencesKey("behavior_hide_ingredient_allocation_action_chips")
val KEY_SETTINGS_BEHAVIOR_INGREDIENTS_SHOW_FRACTIONAL_VALUES =
    booleanPreferencesKey("behavior_ingredients_show_fractional_values")
val KEY_SETTINGS_BEHAVIOR_PROPERTIES_SHOW_FRACTIONAL_VALUES =
    booleanPreferencesKey("behavior_properties_show_fractional_values")

val KEY_SETTINGS_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
val KEY_SETTINGS_TANDOOR_CREDENTIALS = stringPreferencesKey("tandoor_credentials")

val KEY_SETTINGS_LATEST_VERSION_CHECK = stringPreferencesKey("latest_version_check")

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@SuppressLint("StaticFieldLeak")
class LegacySettingsViewModel(
    app: Application, context: Context
) : AndroidViewModel(app) {
    val getLatestVersionCheck: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[KEY_SETTINGS_LATEST_VERSION_CHECK] ?: "" }

    val getOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_SETTINGS_ONBOARDING_COMPLETED] ?: false }

    val getTandoorCredentials: Flow<TandoorCredentials?> = context.dataStore.data
        .map {
            json.maybeDecodeFromString<TandoorCredentials>(
                it[KEY_SETTINGS_TANDOOR_CREDENTIALS] ?: ""
            )
        }

    val getEnableDynamicColors: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_SETTINGS_APPEARANCE_DYNAMIC_COLORS] ?: true }

    val getEnableSystemTheme: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_SETTINGS_APPEARANCE_SYSTEM_THEME] ?: true }

    val getEnableDarkTheme: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_SETTINGS_APPEARANCE_DARK_THEME] ?: false }

    val getUseShareWrapper: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_SETTINGS_BEHAVIOR_USE_SHARE_WRAPPER] ?: true }

    val getUseShareWrapperHintShown: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_SETTINGS_BEHAVIOR_USE_SHARE_WRAPPER_HINT_SHOWN] ?: ""
        }

    val getHideIngredientAllocationActionChips: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_SETTINGS_BEHAVIOR_HIDE_INGREDIENT_ALLOCATION_ACTION_CHIPS] ?: false
        }

    val getIngredientsShowFractionalValues: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_SETTINGS_BEHAVIOR_INGREDIENTS_SHOW_FRACTIONAL_VALUES] ?: true
        }

    val getPropertiesShowFractionalValues: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_SETTINGS_BEHAVIOR_PROPERTIES_SHOW_FRACTIONAL_VALUES] ?: true
        }
}