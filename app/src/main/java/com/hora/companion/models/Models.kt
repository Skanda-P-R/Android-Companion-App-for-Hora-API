package com.hora.companion.models

import com.squareup.moshi.Json

data class AllResponse(
    val data: Map<String, Any> = emptyMap()
)

data class KundaliResponse(
    val data: Map<String, Any> = emptyMap()
)

data class LocationData(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String? = null,
    val description: String? = null
)

data class PanchangaLimb(
    val name: String,
    val number: Int,
    val progress: Double,
    @Json(name = "longitude_degrees") val longitudeDegrees: Double,
    @Json(name = "ends_at") val endsAt: String? = null
)

data class MuhurtaInterval(
    val name: String,
    val start: String,
    val end: String,
    val display: String,
    @Json(name = "duration_seconds") val durationSeconds: Double
)
