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

// Base fields shared by most responses
data class BaseApiResponse(
    val date: String,
    @Json(name = "local_date") val localDate: String,
    @Json(name = "vedic_day_date") val vedicDayDate: String,
    val datetime: String,
    val timezone: String,
    val location: String,
    val coordinates: Map<String, Double>,
    val ayanamsa: String
)

data class PanchangaResponse(
    val date: String,
    @Json(name = "vedic_day_date") val vedicDayDate: String,
    val panchanga: Map<String, String>,
    @Json(name = "panchanga_details") val panchangaDetails: Map<String, Any>,
    val moon: Map<String, Any>,
    val sun: Map<String, Any>
)

data class MuhurtaResponse(
    val date: String,
    @Json(name = "vedic_day_date") val vedicDayDate: String,
    val sunrise: String,
    val sunset: String,
    @Json(name = "sunrise_at") val sunriseAt: String,
    @Json(name = "sunset_at") val sunsetAt: String,
    val muhurta: Map<String, MuhurtaInterval>
)

data class HoraResponse(
    val date: String,
    @Json(name = "vedic_day_date") val vedicDayDate: String,
    val hora: HoraData
)

data class HoraData(
    val planet: String,
    val symbol: String,
    val number: Int,
    val period: String,
    @Json(name = "period_number") val periodNumber: Int,
    val started: String,
    val ends: String,
    @Json(name = "started_at") val startedAt: String,
    @Json(name = "ends_at") val endsAt: String,
    val remaining: String,
    @Json(name = "remaining_seconds") val remainingSeconds: Int,
    val next: String
)

data class DayResponse(
    val date: String,
    @Json(name = "vedic_day_date") val vedicDayDate: String,
    val sunrise: String,
    val sunset: String,
    @Json(name = "sunrise_at") val sunriseAt: String,
    @Json(name = "sunset_at") val sunsetAt: String,
    @Json(name = "next_sunrise_at") val nextSunriseAt: String,
    @Json(name = "solar_noon_at") val solarNoonAt: String,
    @Json(name = "daylight_midpoint_at") val daylightMidpointAt: String,
    @Json(name = "day_duration_seconds") val dayDurationSeconds: Double,
    @Json(name = "night_duration_seconds") val nightDurationSeconds: Double,
    val vara: String,
    @Json(name = "vara_sanskrit") val varaSanskrit: String
)

data class MuhurtaInterval(
    val name: String,
    val start: String,
    val end: String,
    val display: String,
    @Json(name = "duration_seconds") val durationSeconds: Double
)
