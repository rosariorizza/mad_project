package it.polito.mad.g18.mad_lab5.gui

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "settings")

class ThemePreferences(context: Context) {

    private val THEME_KEY = booleanPreferencesKey("theme_preference")
    private val dataStore = context.dataStore

    val darkTheme: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: false
    }

    suspend fun setDarkTheme(darkTheme: Boolean) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = darkTheme
        }
    }
}

