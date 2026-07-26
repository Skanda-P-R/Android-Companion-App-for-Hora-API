package com.hora.companion.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hora.companion.repository.HoraRepository
import com.hora.companion.models.DashaResponse
import com.hora.companion.models.DashaPeriod
import android.content.Context
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.*

data class TransitState(
    val isLoading: Boolean = false,
    val dashaResponse: DashaResponse? = null,
    val chartUrl: String? = null,
    val error: String? = null
)

class TransitViewModel(
    private val repo: HoraRepository,
    private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(TransitState())
    val state: StateFlow<TransitState> = _state

    private val _chartLoaded = MutableStateFlow(false)
    val chartLoaded: StateFlow<Boolean> = _chartLoaded

    // Hierarchical navigation for Dasha tab
    private val _selectedL1 = MutableStateFlow<DashaPeriod?>(null)
    val selectedL1: StateFlow<DashaPeriod?> = _selectedL1

    private val _selectedL2 = MutableStateFlow<DashaPeriod?>(null)
    val selectedL2: StateFlow<DashaPeriod?> = _selectedL2

    fun fetchData(
        lat: Double?,
        lon: Double?,
        location: String?,
        date: String,
        time: String,
        lang: String,
        apiBase: String,
        depth: Int,
        chartStyle: String,
        sessionToken: String?
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            _chartLoaded.value = false
            _selectedL1.value = null
            _selectedL2.value = null

            val apiLang = if (lang == "kn") "kan" else "en"
            val normalizedBase = if (apiBase.endsWith("/")) apiBase else "$apiBase/"

            coroutineScope {
                val chartUrl = buildString {
                    append("${normalizedBase}api/v1/kundali/svg?")
                    if (location != null) {
                        append("location=${java.net.URLEncoder.encode(location, "UTF-8")}")
                    } else if (lat != null && lon != null) {
                        append("lat=$lat&lon=$lon")
                    } else {
                        append("lat=12.9716&lon=77.5946")
                    }
                    append("&date=$date")
                    append("&time=$time")
                    append("&lang=$apiLang")
                    append("&chart_style=$chartStyle")
                }

                // Pre-fetch chart image
                val imageRequest = ImageRequest.Builder(context)
                    .data(chartUrl)
                    .apply {
                        if (sessionToken != null) {
                            addHeader("Authorization", "Bearer $sessionToken")
                        }
                    }
                    .build()
                
                val chartJob = async {
                    context.imageLoader.execute(imageRequest)
                    _chartLoaded.value = true
                }

                val dashaDeferred = async { 
                    repo.fetchDasha(lat, lon, location, date, time, lang, depth)
                }

                val dashaResult = dashaDeferred.await()
                chartJob.await()

                if (dashaResult.isSuccess) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        dashaResponse = dashaResult.getOrNull(),
                        chartUrl = chartUrl
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = dashaResult.exceptionOrNull()?.message ?: "Unknown error",
                        chartUrl = chartUrl
                    )
                }
            }
        }
    }

    fun selectL1(period: DashaPeriod?) {
        _selectedL1.value = period
        _selectedL2.value = null
    }

    fun selectL2(period: DashaPeriod?) {
        _selectedL2.value = period
    }

    fun formatDecimalYears(decimalYears: Double): String {
        val years = decimalYears.toInt()
        val remainingAfterYears = (decimalYears - years) * 12
        val months = remainingAfterYears.toInt()
        val remainingAfterMonths = (remainingAfterYears - months) * 30
        val days = remainingAfterMonths.toInt()
        
        return "${years}y ${months}m ${days}d"
    }
}
