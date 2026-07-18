package com.hora.companion.security

import android.content.Context
import android.provider.Settings
import java.util.UUID

class DeviceUuidProvider(private val context: Context) {
    fun getDeviceUuid(): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        
        return if (!androidId.isNullOrEmpty()) {
            // Convert ANDROID_ID to a UUID format for consistency with the server
            UUID.nameUUIDFromBytes(androidId.toByteArray()).toString()
        } else {
            // Fallback to random if ANDROID_ID is missing (rare)
            UUID.randomUUID().toString()
        }
    }
}
