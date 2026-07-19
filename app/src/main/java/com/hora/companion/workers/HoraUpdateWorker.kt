package com.hora.companion.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hora.companion.api.HoraApiService
import com.hora.companion.repository.HoraRepository
import com.hora.companion.DataStoreManager
import com.hora.companion.data.AuthRepository
import com.hora.companion.utils.WidgetUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async

class HoraUpdateWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = coroutineScope {
        val dataStoreManager = DataStoreManager(applicationContext)
        val authRepository = AuthRepository(applicationContext)
        
        val token = authRepository.getSessionTokenBlocking()
        if (token == null) {
            return@coroutineScope Result.failure()
        }

        val api = HoraApiService.create(
            authRepository = authRepository,
            onSessionExpired = {
                authRepository.notifySessionExpired() 
            }
        )
        val repo = HoraRepository(api, applicationContext)
        
        val location = dataStoreManager.locationFlow.first()
        val locationName = dataStoreManager.locationNameFlow.first()
        val lang = dataStoreManager.langFlow.first()
        
        try {
            val lat = location?.first
            val lon = location?.second

            // Update Hora (frequent)
            val horaDeferred = async { repo.fetchHora(lat, lon, locationName, lang = lang) }
            
            // Update Daily parts (repo handles once-per-day caching internally)
            val panDeferred = async { repo.fetchPanchanga(lat, lon, locationName, lang = lang) }
            val muhDeferred = async { repo.fetchMuhurta(lat, lon, locationName, lang = lang) }
            val dayDeferred = async { repo.fetchDay(lat, lon, locationName, lang = lang) }
            
            // Kundali (required for widget)
            val kundaliDeferred = async { repo.fetchKundaliImage(lat, lon, locationName, lang = lang) }

            val hRes = horaDeferred.await()
            panDeferred.await()
            muhDeferred.await()
            dayDeferred.await()
            kundaliDeferred.await()

            if (hRes.isSuccess) {
                WidgetUtils.updateAllWidgets(applicationContext)
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
