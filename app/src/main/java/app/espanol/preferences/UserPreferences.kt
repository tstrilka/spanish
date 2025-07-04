package app.espanol.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val SPEECH_RATE = stringPreferencesKey("speech_rate")
        val AUTO_SAVE = booleanPreferencesKey("auto_save")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    val speechRate: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[SPEECH_RATE]?.toFloatOrNull() ?: 1.0f
    }

    val autoSave: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_SAVE] ?: false
    }

    val darkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE] ?: false
    }

    suspend fun setSpeechRate(rate: Float) {
        context.dataStore.edit { preferences ->
            preferences[SPEECH_RATE] = rate.toString()
        }
    }

    suspend fun setAutoSave(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_SAVE] = enabled
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE] = enabled
        }
    }
}