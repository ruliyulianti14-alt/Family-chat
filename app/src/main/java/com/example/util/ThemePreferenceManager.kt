package com.example.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.AppThemeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.familyPrefsDataStore by preferencesDataStore(name = "my_family_chat_prefs")

object ThemePreferenceManager {
    private val SELECTED_THEME_KEY = stringPreferencesKey("selected_theme_id")

    fun observeTheme(context: Context): Flow<AppThemeOption> =
        context.applicationContext.familyPrefsDataStore.data.map { prefs ->
            AppThemeOption.fromId(prefs[SELECTED_THEME_KEY])
        }

    suspend fun setTheme(context: Context, option: AppThemeOption) {
        context.applicationContext.familyPrefsDataStore.edit { prefs ->
            prefs[SELECTED_THEME_KEY] = option.id
        }
    }
}
