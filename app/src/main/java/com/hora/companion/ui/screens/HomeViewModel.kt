package com.hora.companion.ui.screens

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hora.companion.repository.HoraRepository
import com.hora.companion.utils.WidgetUtils
import com.hora.companion.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class HomeViewModel(private val repo: HoraRepository) : ViewModel() {
    private val _state = MutableStateFlow(PanchangaState(isLoading = true))
    val state: StateFlow<PanchangaState> = _state

    fun refresh(context: Context, lat: Double?, lon: Double?, locationName: String? = null, force: Boolean = false) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val dataStore = DataStoreManager(context)
                val lang = dataStore.langFlow.first()

                coroutineScope {
                    val panDeferred = async { repo.fetchPanchanga(lat, lon, locationName, lang = lang, force = force) }
                    val muhDeferred = async { repo.fetchMuhurta(lat, lon, locationName, lang = lang, force = force) }
                    val dayDeferred = async { repo.fetchDay(lat, lon, locationName, lang = lang, force = force) }
                    val horaDeferred = async { repo.fetchHora(lat, lon, locationName, lang = lang, force = force) }
                    val kundaliDeferred = async { repo.fetchKundaliImage(lat, lon, locationName, lang = lang) }

                    val panRes = panDeferred.await()
                    val muhRes = muhDeferred.await()
                    val dayRes = dayDeferred.await()
                    val horaRes = horaDeferred.await()
                    kundaliDeferred.await()

                    val merged = repo.mergeToState(
                        panchangaJson = panRes.getOrNull(),
                        muhurtaJson = muhRes.getOrNull(),
                        dayJson = dayRes.getOrNull(),
                        horaJson = horaRes.getOrNull()
                    )

                    _state.value = merged.copy(
                        isLoading = false,
                        error = if (panRes.isFailure) panRes.exceptionOrNull()?.message else null
                    )
                    
                    if (panRes.isSuccess || horaRes.isSuccess) {
                        WidgetUtils.updateAllWidgets(context.applicationContext)
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error refreshing", e)
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun refreshHoraOnly(context: Context, lat: Double?, lon: Double?, locationName: String? = null) {
        viewModelScope.launch {
            val dataStore = DataStoreManager(context)
            val lang = dataStore.langFlow.first()
            val res = repo.fetchHora(lat, lon, locationName, lang = lang, force = false)
            if (res.isSuccess) {
                val newState = repo.mergeToState(
                    panchangaJson = null,
                    muhurtaJson = null,
                    dayJson = null,
                    horaJson = res.getOrNull()
                )
                // Merge with current state fields that are NOT hora-related
                _state.value = _state.value.copy(
                    hora = newState.hora,
                    horaSymbol = newState.horaSymbol,
                    horaNext = newState.horaNext,
                    horaEnds = newState.horaEnds,
                    horaEndsAt = newState.horaEndsAt,
                    remaining = newState.remaining
                )
            }
        }
    }
}
