package com.example.appbase.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsDataStore(private val context: Context) {
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
        
        // Preference keys
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val PUSH_NOTIFICATIONS_ENABLED = booleanPreferencesKey("push_notifications_enabled")
        private val EMAIL_NOTIFICATIONS_ENABLED = booleanPreferencesKey("email_notifications_enabled")
        private val DARK_THEME_ENABLED = booleanPreferencesKey("dark_theme_enabled")
        private val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        private val AUTO_THEME_ENABLED = booleanPreferencesKey("auto_theme_enabled")
    }
    
    // Notification preferences
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[NOTIFICATIONS_ENABLED] ?: true
        }
    
    val pushNotificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PUSH_NOTIFICATIONS_ENABLED] ?: true
        }
    
    val emailNotificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[EMAIL_NOTIFICATIONS_ENABLED] ?: false
        }
    
    // Theme preferences
    val darkThemeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_THEME_ENABLED] ?: false
        }
    
    val autoThemeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[AUTO_THEME_ENABLED] ?: true
        }
    
    // Language preferences
    val selectedLanguage: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SELECTED_LANGUAGE] ?: "en"
        }
    
    // Save notification preferences
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }
    
    suspend fun setPushNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PUSH_NOTIFICATIONS_ENABLED] = enabled
        }
    }
    
    suspend fun setEmailNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[EMAIL_NOTIFICATIONS_ENABLED] = enabled
        }
    }
    
    // Save theme preferences
    suspend fun setDarkThemeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_THEME_ENABLED] = enabled
        }
    }
    
    suspend fun setAutoThemeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_THEME_ENABLED] = enabled
        }
    }
    
    // Save language preference
    suspend fun setSelectedLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_LANGUAGE] = language
        }
    }
    
    // Get all settings as a map
    val allSettings: Flow<Map<String, Any>> = context.dataStore.data
        .map { preferences ->
            mapOf(
                "notifications_enabled" to (preferences[NOTIFICATIONS_ENABLED] ?: true),
                "push_notifications_enabled" to (preferences[PUSH_NOTIFICATIONS_ENABLED] ?: true),
                "email_notifications_enabled" to (preferences[EMAIL_NOTIFICATIONS_ENABLED] ?: false),
                "dark_theme_enabled" to (preferences[DARK_THEME_ENABLED] ?: false),
                "auto_theme_enabled" to (preferences[AUTO_THEME_ENABLED] ?: true),
                "selected_language" to (preferences[SELECTED_LANGUAGE] ?: "en")
            )
        }
}

