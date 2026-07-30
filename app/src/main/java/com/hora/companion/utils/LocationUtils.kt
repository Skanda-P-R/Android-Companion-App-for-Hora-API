package com.hora.companion.utils

import java.util.Locale

import kotlin.math.abs

object LocationUtils {
    fun formatCoord(value: Double?): String? {
        if (value == null) return null
        return "%.4f".format(Locale.US, value)
    }

    fun isSignificantChange(oldLat: Double?, oldLon: Double?, newLat: Double?, newLon: Double?): Boolean {
        if (oldLat == null || oldLon == null || newLat == null || newLon == null) return true
        return abs(oldLat - newLat) >= 1.0 || abs(oldLon - newLon) >= 1.0
    }
}
