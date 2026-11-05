package com.example.voidstep.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "voidstep_preferences")

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val bestScore: Int = 0,
    val highestStreak: Int = 0
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val BEST_SCORE = intPreferencesKey("best_score")
        val HIGHEST_STREAK = intPreferencesKey("highest_streak")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeModeString = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.DARK.name
            val themeMode = try {
                ThemeMode.valueOf(themeModeString)
            } catch (e: IllegalArgumentException) {
                ThemeMode.DARK
            }
            val soundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: true
            val hapticsEnabled = preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true
            val bestScore = preferences[PreferencesKeys.BEST_SCORE] ?: 0
            val highestStreak = preferences[PreferencesKeys.HIGHEST_STREAK] ?: 0

            UserPreferences(
                themeMode = themeMode,
                soundEnabled = soundEnabled,
                hapticsEnabled = hapticsEnabled,
                bestScore = bestScore,
                highestStreak = highestStreak
            )
        }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun recordGameResults(score: Int, streak: Int): Pair<Boolean, Boolean> {
        var isNewBestScore = false
        var isNewBestStreak = false
        context.dataStore.edit { preferences ->
            val currentBest = preferences[PreferencesKeys.BEST_SCORE] ?: 0
            if (score > currentBest) {
                preferences[PreferencesKeys.BEST_SCORE] = score
                isNewBestScore = true
            }

            val currentStreak = preferences[PreferencesKeys.HIGHEST_STREAK] ?: 0
            if (streak > currentStreak) {
                preferences[PreferencesKeys.HIGHEST_STREAK] = streak
                isNewBestStreak = true
            }
        }
        return Pair(isNewBestScore, isNewBestStreak)
    }

    suspend fun resetBestScore() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BEST_SCORE] = 0
            preferences[PreferencesKeys.HIGHEST_STREAK] = 0
        }
    }
}
