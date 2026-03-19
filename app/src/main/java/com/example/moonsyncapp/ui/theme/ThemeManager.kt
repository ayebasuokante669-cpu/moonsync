package com.example.moonsyncapp.ui.theme

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

class ThemeManager(private val context: Context) {
    companion object {
        private val USE_DARK_MODE = booleanPreferencesKey("use_dark_mode")
        private val USE_SYSTEM_THEME = booleanPreferencesKey("use_system_theme")
    }

    val themeFlow: Flow<ThemePreferences> = context.dataStore.data
        .map { preferences ->
            ThemePreferences(
                useDarkMode = preferences[USE_DARK_MODE] ?: false,
                useSystemTheme = preferences[USE_SYSTEM_THEME] ?: true
            )
        }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_DARK_MODE] = enabled
        }
    }

    suspend fun setSystemTheme(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_SYSTEM_THEME] = enabled
        }
    }
}

data class ThemePreferences(
    val useDarkMode: Boolean = false,
    val useSystemTheme: Boolean = true
)

val LocalThemeManager = compositionLocalOf<ThemeManager?> {
    null
}