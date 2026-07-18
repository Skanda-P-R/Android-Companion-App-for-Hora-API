package com.hora.companion.repository

import android.content.Context
import android.util.Log
import com.hora.companion.api.HoraApiService
import com.hora.companion.CacheManager
import com.hora.companion.ui.screens.PanchangaState
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HoraRepository(private val api: HoraApiService, private val context: Context) {
    private val cache = CacheManager(context)
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

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
        lang: String
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        return@withContext try {
            val apiLang = if (lang == "kn") "kan" else "en"
            val resp = api.getKundaliChartRaw(lat, lon, location, date, time, apiLang)
            val bytes = resp.bytes()
            
            // Basic check to see if it's an image or JSON error
            val firstChars = bytes.take(10).map { it.toInt().toChar() }.joinToString("")
            if (firstChars.contains("{") || firstChars.contains("error")) {
                Log.e("HoraRepository", "Received non-image data for kundali: ${String(bytes)}")
                return@withContext Result.failure(Exception("Server returned error instead of image"))
            }

            if (date == null && time == null) {
                cache.saveBytes("kundali.png", bytes)
            }
            Result.success(bytes)
        } catch (e: Exception) {
            Log.e("HoraRepository", "Error fetching kundali image", e)
            val cached = cache.readBytes("kundali.png")
            if (cached != null && date == null && time == null) Result.success(cached) else Result.failure(e)
        }
    }

    suspend fun fetchBirthChartImage(
        lat: Double? = null,
        lon: Double? = null,
        location: String? = null,
        date: String? = null,
        time: String? = null,
        name: String? = null,
        lang: String
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        return@withContext try {
            val apiLang = if (lang == "kn") "kan" else "en"
            val resp = api.getBirthChartRaw(lat, lon, location, date, time, name, apiLang)
            Result.success(resp.bytes())
        } catch (e: Exception) {
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

    fun parsePanchangaFromJson(json: String): PanchangaState {
        try {
            val adapter = moshi.adapter(Map::class.java)
            val map = adapter.fromJson(json) as? Map<*, *> ?: return PanchangaState()
            
            val horaObj = map["hora"] as? Map<*, *>
            val panSummary = map["panchanga"] as? Map<*, *>
            val panDetails = map["panchanga_details"] as? Map<*, *>
            val moonObj = map["moon"] as? Map<*, *>
            val sunObj = map["sun"] as? Map<*, *>
            
            val muhurtaMap = map["muhurta"] as? Map<*, *>

            fun getEnd(obj: Any?): String {
                val detail = obj as? Map<*, *>
                val endsAt = detail?.get("ends_at")?.toString() ?: ""
                if (endsAt.isEmpty()) return ""
                // Extract just time for display if it's a long ISO string, or keep as is
                // For simplicity, let's just use the raw string or format it
                return endsAt.split("T").lastOrNull()?.take(5) ?: ""
            }

            return PanchangaState(
                hora = horaObj?.get("planet")?.toString() ?: "--",
                horaSymbol = horaObj?.get("symbol")?.toString() ?: "",
                horaNext = horaObj?.get("next")?.toString() ?: "--",
                horaEnds = horaObj?.get("ends")?.toString() ?: "--",
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
                isLoading = false
            )
        } catch (e: Exception) {
            return PanchangaState(isLoading = false, error = e.message)
        }
    }
}
