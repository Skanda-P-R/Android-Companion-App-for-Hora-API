package com.hora.companion.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hora.companion.repository.HoraRepository
import com.hora.companion.utils.WidgetUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import com.hora.companion.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.async

class HomeViewModel(private val repo: HoraRepository) : ViewModel() {
    private val _state = MutableStateFlow(PanchangaState(isLoading = true))
    val state: StateFlow<PanchangaState> = _state

    fun refresh(context: Context, lat: Double, lon: Double) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val dataStore = DataStoreManager(context)
            val lang = dataStore.langFlow.first()

            val panchangaDeferred = async { repo.fetchAllRaw(lat, lon, lang) }
            val kundaliDeferred = async { repo.fetchKundaliImage(lat, lon, lang) }

            val res = panchangaDeferred.await()
            val kundaliRes = kundaliDeferred.await()

            if (res.isSuccess) {
                val json = res.getOrNull() ?: ""
                val parsed = repo.parsePanchangaFromJson(json)
                _state.value = parsed.copy(isLoading = false, error = null)
                // Trigger widget update
                WidgetUtils.updateAllWidgets(context.applicationContext)
            } else {
                _state.value = _state.value.copy(
                    isLoading = false, 
                    error = res.exceptionOrNull()?.message ?: "Unknown Error"
                )
            }
        }
    }
}
