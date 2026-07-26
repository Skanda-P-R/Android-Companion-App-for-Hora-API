package com.hora.companion.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth_settings")

class AuthRepository(private val context: Context) {
    companion object {
        private val KEY_SESSION_TOKEN = stringPreferencesKey("session_token")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_PICTURE = stringPreferencesKey("user_picture")
    }

    private val _sessionExpiredEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpiredEvent = _sessionExpiredEvent.asSharedFlow()

    fun notifySessionExpired() {
        _sessionExpiredEvent.tryEmit(Unit)
    }

    // Read token as a Flow
    val sessionToken: Flow<String?> = context.authDataStore.data.map { preferences ->
        preferences[KEY_SESSION_TOKEN]
    }

    val userEmail: Flow<String?> = context.authDataStore.data.map { preferences ->
        preferences[KEY_USER_EMAIL]
    }

    val userName: Flow<String?> = context.authDataStore.data.map { preferences ->
        preferences[KEY_USER_NAME]
    }

    val userPicture: Flow<String?> = context.authDataStore.data.map { preferences ->
        preferences[KEY_USER_PICTURE]
    }

    // Synchronous helper
    suspend fun getSessionTokenBlocking(): String? {
        return sessionToken.firstOrNull()
    }

    suspend fun saveSessionToken(token: String) {
        context.authDataStore.edit { preferences ->
            preferences[KEY_SESSION_TOKEN] = token
        }
    }

    suspend fun saveUserInfo(email: String, name: String?, picture: String?) {
        context.authDataStore.edit { preferences ->
            preferences[KEY_USER_EMAIL] = email
            if (name != null) preferences[KEY_USER_NAME] = name
            if (picture != null) preferences[KEY_USER_PICTURE] = picture
        }
    }

    suspend fun clearSessionToken() {
        context.authDataStore.edit { preferences ->
            preferences.remove(KEY_SESSION_TOKEN)
            preferences.remove(KEY_USER_EMAIL)
            preferences.remove(KEY_USER_NAME)
            preferences.remove(KEY_USER_PICTURE)
        }
    }
}
