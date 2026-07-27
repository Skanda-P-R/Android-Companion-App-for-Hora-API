package com.hora.companion.repository

import android.content.Context
import android.util.Log
import com.hora.companion.api.HoraApiService
import com.hora.companion.CacheManager
import com.hora.companion.ui.screens.PanchangaState
import com.hora.companion.utils.NetworkUtils
import com.hora.companion.models.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.util.*

class HoraRepository(private val api: HoraApiService, private val context: Context) {
    private val cache = CacheManager(context)
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private fun isToday(dateStr: String?): Boolean {
        if (dateStr == null) return true
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return dateStr == today
    }

    private fun isNow(dateStr: String?, timeStr: String?): Boolean {
        if (dateStr == null && timeStr == null) return true
        val now = Calendar.getInstance()
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        val dateMatch = dateStr == null || dateStr == sdfDate.format(now.time)
        val timeMatch = timeStr == null || timeStr == sdfTime.format(now.time)
        
        // Allow 5 mins grace for time match if specified
        if (dateMatch && timeStr != null) {
            try {
                val parts = timeStr.split(":")
                val h = parts[0].toInt()
                val m = parts[1].toInt()
                val target = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, h)
                    set(Calendar.MINUTE, m)
                }
                return Math.abs(target.timeInMillis - now.timeInMillis) < 300000 // 5 mins
            } catch (e: Exception) {}
        }
        
        return dateMatch && timeMatch
    }

    private fun isVedicDayActive(): Boolean {
        val cached = cache.readJson("day.json") ?: return false
        return try {
            val obj = JSONObject(cached)
            val sunriseAtStr = obj.optString("sunrise_at", "")
            val nextSunriseAtStr = obj.optString("next_sunrise_at", "")
            val vedicDayDate = obj.optString("vedic_day_date", "")
            
            val now = System.currentTimeMillis()
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Primary check: precise timestamps
            if (sunriseAtStr.isNotEmpty() && nextSunriseAtStr.isNotEmpty()) {
                try {
                    val start = ZonedDateTime.parse(sunriseAtStr.replace(" ", "T")).toInstant().toEpochMilli()
                    val end = ZonedDateTime.parse(nextSunriseAtStr.replace(" ", "T")).toInstant().toEpochMilli()
                    if (now in (start - 60000)..(end - 60000)) return true
                } catch (e: Exception) {
                    Log.e("HoraRepository", "Timestamp parse error: ${e.message}")
                }
            }
            
            // Secondary check: is it the same calendar day? 
            // (Vedic day usually overlaps with today's calendar date for the most part)
            if (vedicDayDate.isNotEmpty()) {
                return vedicDayDate == todayDate
            }
            
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun isFresh(cacheName: String, minutes: Int): Boolean {
        val lastMod = cache.lastModified(cacheName)
        if (lastMod == 0L) return false
        val diff = System.currentTimeMillis() - lastMod
        return diff < (minutes * 60 * 1000L)
    }

    suspend fun fetchPanchanga(
        lat: Double? = null,
        lon: Double? = null,
        location: String? = null,
        date: String? = null,
        lang: String,
        force: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        val cacheName = "panchanga.json"
        val online = NetworkUtils.isOnline(context)
        val today = isToday(date)
        val isCurrentVedicDay = today && isVedicDayActive()

        if (!force && isCurrentVedicDay) {
            val cached = cache.readJson(cacheName)
            if (cached != null) return@withContext Result.success(cached)
        }

        if (online) {
            return@withContext try {
                val apiLang = if (lang == "kn") "kan" else "en"
                val json = api.getPanchanga(lat, lon, location, date, apiLang).string()
                if (today) cache.saveJson(cacheName, json)
                Result.success(json)
            } catch (e: Exception) {
                val cached = cache.readJson(cacheName)
                if (today && cached != null) Result.success(cached) else Result.failure(e)
            }
        } else {
            val cached = cache.readJson(cacheName)
            return@withContext if (today && cached != null) Result.success(cached) 
            else Result.failure(Exception("Offline and no cache"))
        }
    }

    suspend fun fetchMuhurta(
        lat: Double? = null,
        lon: Double? = null,
        location: String? = null,
        date: String? = null,
        lang: String,
        force: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        val cacheName = "muhurta.json"
        val online = NetworkUtils.isOnline(context)
        val today = isToday(date)
        val isCurrentVedicDay = today && isVedicDayActive()

        if (!force && isCurrentVedicDay) {
            val cached = cache.readJson(cacheName)
            if (cached != null) return@withContext Result.success(cached)
        }

        if (online) {
            return@withContext try {
                val apiLang = if (lang == "kn") "kan" else "en"
                val json = api.getMuhurta(lat, lon, location, date, apiLang).string()
                if (today) cache.saveJson(cacheName, json)
                Result.success(json)
            } catch (e: Exception) {
                val cached = cache.readJson(cacheName)
                if (today && cached != null) Result.success(cached) else Result.failure(e)
            }
        } else {
            val cached = cache.readJson(cacheName)
            return@withContext if (today && cached != null) Result.success(cached) 
            else Result.failure(Exception("Offline and no cache"))
        }
    }

    suspend fun fetchDay(
        lat: Double? = null,
        lon: Double? = null,
        location: String? = null,
        date: String? = null,
        lang: String,
        force: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        val cacheName = "day.json"
        val online = NetworkUtils.isOnline(context)
        val today = isToday(date)
        val isCurrentVedicDay = today && isVedicDayActive()

        if (!force && isCurrentVedicDay) {
            val cached = cache.readJson(cacheName)
            if (cached != null) return@withContext Result.success(cached)
        }

        if (online) {
            return@withContext try {
                val apiLang = if (lang == "kn") "kan" else "en"
                val json = api.getDay(lat, lon, location, date, apiLang).string()
                if (today) cache.saveJson(cacheName, json)
                Result.success(json)
            } catch (e: Exception) {
                val cached = cache.readJson(cacheName)
                if (today && cached != null) Result.success(cached) else Result.failure(e)
            }
        } else {
            val cached = cache.readJson(cacheName)
            return@withContext if (today && cached != null) Result.success(cached) 
            else Result.failure(Exception("Offline and no cache"))
        }
    }

    suspend fun fetchHora(
        lat: Double? = null,
        lon: Double? = null,
        location: String? = null,
        date: String? = null,
        time: String? = null,
        lang: String,
        force: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        val cacheName = "hora.json"
        val online = NetworkUtils.isOnline(context)
        val isNow = isNow(date, time)

        if (!force && isNow) {
            val cached = cache.readJson(cacheName)
            if (cached != null) {
                try {
                    val obj = JSONObject(cached)
                    val hora = obj.getJSONObject("hora")
                    val endsAtStr = hora.getString("ends_at")
                    val endsAt = ZonedDateTime.parse(endsAtStr).toInstant().toEpochMilli()
                    val now = System.currentTimeMillis()
                    
                    val freshEnough = isFresh(cacheName, 30)
                    
                    if (online) {
                        if (now < endsAt && freshEnough) {
                            return@withContext Result.success(cached)
                        }
                    } else {
                        // Offline: use cache even if expired
                        return@withContext Result.success(cached)
                    }
                } catch (e: Exception) {}
            }
        }

        if (online) {
            return@withContext try {
                val apiLang = if (lang == "kn") "kan" else "en"
                val json = api.getHora(lat, lon, location, date, time, apiLang).string()
                if (isNow) cache.saveJson(cacheName, json)
                Result.success(json)
            } catch (e: Exception) {
                val cached = cache.readJson(cacheName)
                if (isNow && cached != null) Result.success(cached) else Result.failure(e)
            }
        } else {
            val cached = cache.readJson(cacheName)
            return@withContext if (isNow && cached != null) Result.success(cached) 
            else Result.failure(Exception("Offline and no cache or expired"))
        }
    }

    suspend fun fetchAllRaw(
        lat: Double? = null,
        lon: Double? = null,
        location: String? = null,
        date: String? = null,
        time: String? = null,
        lang: String
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val apiLang = if (lang == "kn") "kan" else "en"
            val respBody = api.getAllRaw(lat, lon, location, date, time, apiLang)
            val json = respBody.string()
            if (date == null && time == null) {
                cache.saveJson("all.json", json)
            }
            Result.success(json)
        } catch (e: Exception) {
            Log.e("HoraRepository", "Error fetching all data", e)
            val cached = cache.readJson("all.json")
            if (cached != null && date == null && time == null) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun fetchPanchangaRaw(
        lat: Double? = null,
        lon: Double? = null,
        location: String? = null,
        date: String? = null,
        lang: String
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val apiLang = if (lang == "kn") "kan" else "en"
            val respBody = api.getPanchanga(lat, lon, location, date, apiLang)
            Result.success(respBody.string())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchHoraRaw(
        lat: Double? = null,
        lon: Double? = null,
        location: String? = null,
        date: String? = null,
        time: String? = null,
        lang: String
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val apiLang = if (lang == "kn") "kan" else "en"
            val respBody = api.getHora(lat, lon, location, date, time, apiLang)
            Result.success(respBody.string())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchMuhurtaRaw(
        lat: Double? = null,
        lon: Double? = null,
        location: String? = null,
        date: String? = null,
        lang: String
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val apiLang = if (lang == "kn") "kan" else "en"
            val respBody = api.getMuhurta(lat, lon, location, date, apiLang)
            Result.success(respBody.string())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchKundaliImage(
        lat: Double? = null,
        lon: Double? = null,
        location: String? = null,
        date: String? = null,
        time: String? = null,
        lang: String,
        chartStyle: String? = null,
        force: Boolean = false
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        val cacheName = "kundali.png"
        val online = NetworkUtils.isOnline(context)
        val isCurrentMoment = isNow(date, time)

        if (!force && isCurrentMoment) {
            val cached = cache.readBytes(cacheName)
            if (cached != null) {
                if (online) {
                    if (isFresh(cacheName, 15)) return@withContext Result.success(cached)
                } else {
                    return@withContext Result.success(cached)
                }
            }
        }

        if (online) {
            return@withContext try {
                val apiLang = if (lang == "kn") "kan" else "en"
                val resp = api.getKundaliChartRaw(lat, lon, location, date, time, apiLang, chartStyle)
                val bytes = resp.bytes()
                
                // Basic check to see if it's an image or JSON error
                val firstChars = bytes.take(10).map { it.toInt().toChar() }.joinToString("")
                if (firstChars.contains("{") || firstChars.contains("error")) {
                    Log.e("HoraRepository", "Received non-image data for kundali: ${String(bytes)}")
                    return@withContext Result.failure(Exception("Server returned error instead of image"))
                }

                if (isCurrentMoment) {
                    cache.saveBytes("kundali.png", bytes)
                }
                Result.success(bytes)
            } catch (e: Exception) {
                Log.e("HoraRepository", "Error fetching kundali image", e)
                val cached = cache.readBytes("kundali.png")
                if (isCurrentMoment && cached != null) Result.success(cached) else Result.failure(e)
            }
        } else {
            val cached = cache.readBytes("kundali.png")
            return@withContext if (isCurrentMoment && cached != null) Result.success(cached) 
            else Result.failure(Exception("Offline and no cache"))
        }
    }

    suspend fun fetchBirthChartImage(
        lat: Double? = null,
        lon: Double? = null,
        location: String? = null,
        date: String? = null,
        time: String? = null,
        name: String? = null,
        lang: String,
        chartStyle: String? = null
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        return@withContext try {
            val apiLang = if (lang == "kn") "kan" else "en"
            val resp = api.getBirthChartRaw(lat, lon, location, date, time, name, apiLang, chartStyle)
            Result.success(resp.bytes())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchDasha(
        lat: Double? = null,
        lon: Double? = null,
        location: String? = null,
        date: String? = null,
        time: String? = null,
        lang: String,
        depth: Int? = null
    ): Result<DashaResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            val apiLang = if (lang == "kn") "kan" else "en"
            val resp = api.getDasha(lat, lon, location, date, time, apiLang, depth)
            Result.success(resp)
        } catch (e: Exception) {
            Log.e("HoraRepository", "Error fetching dasha", e)
            Result.failure(e)
        }
    }

    suspend fun fetchBirthDasha(
        lat: Double? = null,
        lon: Double? = null,
        location: String? = null,
        date: String? = null,
        time: String? = null,
        lang: String,
        depth: Int? = null
    ): Result<DashaResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            val apiLang = if (lang == "kn") "kan" else "en"
            val resp = api.getBirthDasha(lat, lon, location, date, time, apiLang, depth)
            Result.success(resp)
        } catch (e: Exception) {
            Log.e("HoraRepository", "Error fetching birth dasha", e)
            Result.failure(e)
        }
    }

    suspend fun fetchLocations(): Result<Map<String, Map<String, Any>>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Result.success(api.getLocations())
        } catch (e: Exception) {
            Log.e("HoraRepository", "Error fetching locations", e)
            Result.failure(e)
        }
    }

    suspend fun addLocation(location: Map<String, Any?>): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = api.addLocation(location)
            Result.success(response.string())
        } catch (e: Exception) {
            Log.e("HoraRepository", "Error adding location: $location", e)
            Result.failure(e)
        }
    }

    suspend fun deleteLocation(name: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = api.deleteLocation(name)
            Result.success(response.string())
        } catch (e: Exception) {
            Log.e("HoraRepository", "Error deleting location: $name", e)
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val responseBody = api.logout()
            val json = responseBody.string()
            val obj = JSONObject(json)
            if (obj.optString("status") == "logged_out") {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Logout failed: Unexpected response status"))
            }
        } catch (e: Exception) {
            Log.e("HoraRepository", "Logout error", e)
            Result.failure(e)
        }
    }

    fun mergeToState(
        panchangaJson: String? = null,
        muhurtaJson: String? = null,
        dayJson: String? = null,
        horaJson: String? = null
    ): PanchangaState {
        var state = PanchangaState()
        
        panchangaJson?.let { json ->
            try {
                val adapter = moshi.adapter(PanchangaResponse::class.java)
                val resp = adapter.fromJson(json)
                resp?.let {
                    state = state.copy(
                        tithi = it.panchanga["tithi"] ?: "--",
                        nakshatra = it.panchanga["nakshatra"] ?: "--",
                        yoga = it.panchanga["yoga"] ?: "--",
                        karana = it.panchanga["karana"] ?: "--",
                        vara = it.panchanga["vara"] ?: "--",
                        samvatsara = it.panchanga["samvatsara"] ?: "--",
                        ayana = it.panchanga["ayana"] ?: "--",
                        rutu = it.panchanga["rutu"] ?: "--",
                        masa = it.panchanga["masa"] ?: "--",
                        paksha = it.panchanga["paksha"] ?: "--",
                        
                        tithiEnds = getEnd(it.panchangaDetails["tithi"]),
                        nakshatraEnds = getEnd(it.panchangaDetails["nakshatra"]),
                        yogaEnds = getEnd(it.panchangaDetails["yoga"]),
                        karanaEnds = getEnd(it.panchangaDetails["karana"]),
                        
                        moonRasi = it.moon["rasi"]?.toString() ?: "--",
                        sunRasi = it.sun["rasi"]?.toString() ?: "--"
                    )
                }
            } catch (e: Exception) { Log.e("HoraRepository", "Error merging panchanga", e) }
        }

        muhurtaJson?.let { json ->
            try {
                val adapter = moshi.adapter(MuhurtaResponse::class.java)
                val resp = adapter.fromJson(json)
                resp?.let {
                    state = state.copy(
                        rahuKalam = it.muhurta["rahu_kalam"]?.display ?: "--",
                        gulika = it.muhurta["gulika"]?.display ?: "--",
                        yamaganda = it.muhurta["yamaganda"]?.display ?: "--",
                        abhijit = it.muhurta["abhijit"]?.display ?: "--"
                    )
                }
            } catch (e: Exception) { Log.e("HoraRepository", "Error merging muhurta", e) }
        }

        dayJson?.let { json ->
            try {
                val adapter = moshi.adapter(DayResponse::class.java)
                val resp = adapter.fromJson(json)
                resp?.let {
                    state = state.copy(
                        sunrise = it.sunrise,
                        sunset = it.sunset,
                        sunriseAt = it.sunriseAt,
                        sunsetAt = it.sunsetAt,
                        nextSunriseAt = it.nextSunriseAt,
                        solarNoonAt = it.solarNoonAt,
                        daylightMidpointAt = it.daylightMidpointAt,
                        dayDuration = it.dayDurationSeconds.toString(),
                        nightDuration = it.nightDurationSeconds.toString(),
                        lastUpdated = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date()),
                        lastUpdatedMillis = System.currentTimeMillis()
                    )
                }
            } catch (e: Exception) { Log.e("HoraRepository", "Error merging day", e) }
        }

        horaJson?.let { json ->
            try {
                val adapter = moshi.adapter(HoraResponse::class.java)
                val resp = adapter.fromJson(json)
                resp?.let {
                    state = state.copy(
                        hora = it.hora.planet,
                        horaSymbol = it.hora.symbol,
                        horaNext = it.hora.next,
                        horaEnds = it.hora.ends,
                        horaEndsAt = it.hora.endsAt,
                        remaining = it.hora.remaining
                    )
                }
            } catch (e: Exception) { Log.e("HoraRepository", "Error merging hora", e) }
        }

        return state
    }

    private fun getEnd(obj: Any?): String {
        val detail = obj as? Map<*, *>
        val endsAt = detail?.get("ends_at")?.toString() ?: ""
        if (endsAt.isEmpty()) return ""
        return endsAt.split("T").lastOrNull()?.take(5) ?: ""
    }

    fun parsePanchangaFromJson(json: String): PanchangaState {
        try {
            val adapter = moshi.adapter(Map::class.java)
            val map = adapter.fromJson(json) as? Map<*, *> ?: return PanchangaState()
            
            val horaObj = map["hora"] as? Map<*, *>
            val panSummary = map["panchanga"] as? Map<*, *>
            val panDetails = map["panchanga_details"] as? Map<*, *>
            val moonObj = map["moon"] as? Map<*, *>
            val sunObj = map["sun"] as? Map<*, *>
            
            return PanchangaState(
                hora = horaObj?.get("planet")?.toString() ?: "--",
                horaSymbol = horaObj?.get("symbol")?.toString() ?: "",
                horaNext = horaObj?.get("next")?.toString() ?: "--",
                horaEnds = horaObj?.get("ends")?.toString() ?: "--",
                horaEndsAt = horaObj?.get("ends_at")?.toString(),
                remaining = horaObj?.get("remaining")?.toString() ?: "--",
                
                tithi = panSummary?.get("tithi")?.toString() ?: "--",
                tithiEnds = getEnd(panDetails?.get("tithi")),
                nakshatra = panSummary?.get("nakshatra")?.toString() ?: "--",
                nakshatraEnds = getEnd(panDetails?.get("nakshatra")),
                yoga = panSummary?.get("yoga")?.toString() ?: "--",
                yogaEnds = getEnd(panDetails?.get("yoga")),
                karana = panSummary?.get("karana")?.toString() ?: "--",
                karanaEnds = getEnd(panDetails?.get("karana")),
                vara = panSummary?.get("vara")?.toString() ?: "--",
                
                samvatsara = panSummary?.get("samvatsara")?.toString() ?: "--",
                ayana = panSummary?.get("ayana")?.toString() ?: "--",
                rutu = panSummary?.get("rutu")?.toString() ?: "--",
                masa = panSummary?.get("masa")?.toString() ?: "--",
                paksha = panSummary?.get("paksha")?.toString() ?: "--",
                
                rahuKalam = map["rahu_kalam"]?.toString() ?: "--",
                gulika = map["gulika"]?.toString() ?: "--",
                yamaganda = map["yamaganda"]?.toString() ?: "--",
                abhijit = map["abhijit"]?.toString() ?: "--",
                
                sunrise = map["sunrise"]?.toString() ?: "--",
                sunset = map["sunset"]?.toString() ?: "--",
                sunriseAt = map["sunrise_at"]?.toString() ?: "--",
                sunsetAt = map["sunset_at"]?.toString() ?: "--",
                nextSunriseAt = map["next_sunrise_at"]?.toString() ?: "--",
                solarNoonAt = map["solar_noon_at"]?.toString() ?: "--",
                daylightMidpointAt = map["daylight_midpoint_at"]?.toString() ?: "--",
                dayDuration = map["day_duration_seconds"]?.toString() ?: "--",
                nightDuration = map["night_duration_seconds"]?.toString() ?: "--",
                
                moonRasi = moonObj?.get("rasi")?.toString() ?: "--",
                sunRasi = sunObj?.get("rasi")?.toString() ?: "--",
                lastUpdated = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date()),
                lastUpdatedMillis = System.currentTimeMillis(),
                isLoading = false
            )
        } catch (e: Exception) {
            return PanchangaState(isLoading = false, error = e.message)
        }
    }
}
