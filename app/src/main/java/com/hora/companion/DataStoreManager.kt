package com.hora.companion

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("hora_prefs")

class DataStoreManager(private val context: Context) {
    companion object {
        val KEY_LAT = doublePreferencesKey("key_lat")
        val KEY_LON = doublePreferencesKey("key_lon")
        val KEY_API_BASE = stringPreferencesKey("key_api_base")
        val KEY_LANG = stringPreferencesKey("key_lang")
    }

    val locationFlow: Flow<Pair<Double, Double>?> = context.dataStore.data.map { prefs ->
        val lat = prefs[KEY_LAT]
        val lon = prefs[KEY_LON]
        if (lat != null && lon != null) lat to lon else null
    }

    val langFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LANG] ?: "en"
    }

    suspend fun saveLocation(lat: Double, lon: Double) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAT] = lat
            prefs[KEY_LON] = lon
        }
    }

    suspend fun getApiBase(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[KEY_API_BASE]
    }

    suspend fun saveApiBase(url: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_API_BASE] = url
        }
    }

    suspend fun getLang(): String {
        val prefs = context.dataStore.data.first()
        return prefs[KEY_LANG] ?: "en"
    }

    suspend fun saveLang(lang: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANG] = lang
        }
    }
}
