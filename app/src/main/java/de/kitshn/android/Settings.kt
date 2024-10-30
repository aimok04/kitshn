package de.kitshn.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import de.kitshn.android.api.tandoor.TandoorCredentials
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString

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
class SettingsViewModel(
    app: Application, val context: Context
) : AndroidViewModel(app) {

    // latest version check
    val getLatestVersionCheck: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[KEY_SETTINGS_LATEST_VERSION_CHECK] ?: "" }

    suspend fun setLatestVersionCheck(version: String) = context.dataStore.edit {
        it[KEY_SETTINGS_LATEST_VERSION_CHECK] = version
    }

    // onboarding
    val getOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_SETTINGS_ONBOARDING_COMPLETED] ?: false }

    suspend fun setOnboardingCompleted(done: Boolean) = context.dataStore.edit {
        it[KEY_SETTINGS_ONBOARDING_COMPLETED] = done
    }

    // credentials
    val getTandoorCredentials: Flow<TandoorCredentials?> = context.dataStore.data
        .map {
            json.maybeDecodeFromString<TandoorCredentials>(
                it[KEY_SETTINGS_TANDOOR_CREDENTIALS] ?: ""
            )
        }

    suspend fun saveTandoorCredentials(credentials: TandoorCredentials?) = context.dataStore.edit {
        it[KEY_SETTINGS_TANDOOR_CREDENTIALS] =
            if(credentials == null) "" else json.encodeToString(credentials)
    }

    // design
    val getEnableDynamicColors: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_SETTINGS_APPEARANCE_DYNAMIC_COLORS] ?: true }

    val getEnableSystemTheme: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_SETTINGS_APPEARANCE_SYSTEM_THEME] ?: true }

    val getEnableDarkTheme: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_SETTINGS_APPEARANCE_DARK_THEME] ?: false }

    suspend fun setEnableDynamicColors(enable: Boolean) = context.dataStore.edit {
        it[KEY_SETTINGS_APPEARANCE_DYNAMIC_COLORS] = enable
    }

    suspend fun setEnableSystemTheme(enable: Boolean) = context.dataStore.edit {
        it[KEY_SETTINGS_APPEARANCE_SYSTEM_THEME] = enable
    }

    suspend fun setEnableDarkTheme(enable: Boolean) = context.dataStore.edit {
        it[KEY_SETTINGS_APPEARANCE_DARK_THEME] = enable
    }

    //behavior
    val getUseShareWrapper: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_SETTINGS_BEHAVIOR_USE_SHARE_WRAPPER] ?: true }

    suspend fun setUseShareWrapper(use: Boolean) = context.dataStore.edit {
        it[KEY_SETTINGS_BEHAVIOR_USE_SHARE_WRAPPER] = use
    }

    val getUseShareWrapperHintShown: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_SETTINGS_BEHAVIOR_USE_SHARE_WRAPPER_HINT_SHOWN] ?: ""
        }

    suspend fun setUseShareWrapperHintShown(url: String) = context.dataStore.edit {
        it[KEY_SETTINGS_BEHAVIOR_USE_SHARE_WRAPPER_HINT_SHOWN] = url
    }

    val getHideIngredientAllocationActionChips: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_SETTINGS_BEHAVIOR_HIDE_INGREDIENT_ALLOCATION_ACTION_CHIPS] ?: false
        }

    suspend fun setHideIngredientAllocationActionChips(hide: Boolean) = context.dataStore.edit {
        it[KEY_SETTINGS_BEHAVIOR_HIDE_INGREDIENT_ALLOCATION_ACTION_CHIPS] = hide
    }

    val getIngredientsShowFractionalValues: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_SETTINGS_BEHAVIOR_INGREDIENTS_SHOW_FRACTIONAL_VALUES] ?: true
        }

    suspend fun setIngredientsShowFractionalValues(show: Boolean) = context.dataStore.edit {
        it[KEY_SETTINGS_BEHAVIOR_INGREDIENTS_SHOW_FRACTIONAL_VALUES] = show
    }

    val getPropertiesShowFractionalValues: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_SETTINGS_BEHAVIOR_PROPERTIES_SHOW_FRACTIONAL_VALUES] ?: true
        }

    suspend fun setPropertiesShowFractionalValues(show: Boolean) = context.dataStore.edit {
        it[KEY_SETTINGS_BEHAVIOR_PROPERTIES_SHOW_FRACTIONAL_VALUES] = show
    }

}