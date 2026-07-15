package com.hora.companion.repository

import android.content.Context
import com.hora.companion.api.HoraApiService
import com.hora.companion.CacheManager
import com.hora.companion.ui.screens.PanchangaState
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HoraRepository(private val api: HoraApiService, private val context: Context) {
    private val cache = CacheManager(context)
    private val moshi = Moshi.Builder().build()

    suspend fun fetchAllRaw(lat: Double, lon: Double): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val respBody = api.getAllRaw(lat, lon)
            val json = respBody.string()
            cache.saveJson("all.json", json)
            Result.success(json)
        } catch (e: Exception) {
            val cached = cache.readJson("all.json")
            if (cached != null) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun fetchKundaliImage(lat: Double, lon: Double, lang: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        return@withContext try {
            val apiLang = if (lang == "kn") "kan" else "en"
            val resp = api.getKundaliChartRaw(lat, lon, apiLang)
            val bytes = resp.bytes()
            cache.saveBytes("kundali.png", bytes)
            Result.success(bytes)
        } catch (e: Exception) {
            val cached = cache.readBytes("kundali.png")
            if (cached != null) Result.success(cached) else Result.failure(e)
        }
    }

    fun parsePanchangaFromJson(json: String): PanchangaState {
        try {
            val adapter = moshi.adapter(Map::class.java)
            val map = adapter.fromJson(json) as? Map<*, *> ?: return PanchangaState()
            
            val horaObj = map["hora"] as? Map<*, *>
            val panObj = map["panchanga"] as? Map<*, *>
            val moonObj = map["moon"] as? Map<*, *>
            val sunObj = map["sun"] as? Map<*, *>

            val endsAtRaw = horaObj?.get("ends_at")?.toString()
            val formattedEnds = try {
                if (endsAtRaw != null) {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val date = inputFormat.parse(endsAtRaw.take(19))
                    if (date != null) outputFormat.format(date) else "--:--"
                } else "--:--"
            } catch (e: Exception) {
                "--:--"
            }

            return PanchangaState(
                hora = horaObj?.get("planet")?.toString() ?: "--",
                horaSymbol = horaObj?.get("symbol")?.toString() ?: "",
                horaNext = horaObj?.get("next")?.toString() ?: "--",
                horaEnds = formattedEnds,
                remaining = horaObj?.get("remaining")?.toString() ?: "--",
                tithi = panObj?.get("tithi")?.toString() ?: "--",
                nakshatra = panObj?.get("nakshatra")?.toString() ?: "--",
                yoga = panObj?.get("yoga")?.toString() ?: "--",
                karana = panObj?.get("karana")?.toString() ?: "--",
                vara = panObj?.get("vara")?.toString() ?: "--",
                rahuKalam = map["rahu_kalam"]?.toString() ?: "--",
                yamaganda = map["yamaganda"]?.toString() ?: "--",
                abhijit = map["abhijit"]?.toString() ?: "--",
                sunrise = map["sunrise"]?.toString() ?: "--",
                sunset = map["sunset"]?.toString() ?: "--",
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
