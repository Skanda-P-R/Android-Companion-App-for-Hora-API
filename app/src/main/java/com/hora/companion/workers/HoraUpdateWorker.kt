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

class HoraUpdateWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val dataStoreManager = DataStoreManager(applicationContext)
        val authRepository = AuthRepository(applicationContext)
        
        authRepository.getSessionTokenBlocking() ?: return Result.failure()

        val location = dataStoreManager.locationFlow.first()
        val locationName = dataStoreManager.locationNameFlow.first()
        val lang = dataStoreManager.langFlow.first()
        val apiBase = dataStoreManager.apiBaseFlow.first()
        
        try {
            val api = HoraApiService.create(
                authRepository = authRepository,
                onSessionExpired = {
                    authRepository.notifySessionExpired()
                },
                baseUrl = apiBase
            )
            val repo = HoraRepository(api, applicationContext)
            
            val lat = location?.first
            val lon = location?.second

            // These will only hit the network if the Vedic Day has changed
            repo.fetchDay(lat, lon, locationName, lang = lang, force = false)
            repo.fetchPanchanga(lat, lon, locationName, lang = lang, force = false)
            repo.fetchMuhurta(lat, lon, locationName, lang = lang, force = false)
            repo.fetchHora(lat, lon, locationName, lang = lang, force = false)
            
            // Force Kundali update to keep the chart fresh on the widget
            val kundaliRes = repo.fetchKundaliImage(lat, lon, locationName, lang = lang, force = true)

            return if (kundaliRes.isSuccess) {
                WidgetUtils.updateAllWidgets(applicationContext)
                Result.success()
            } else {
                Result.retry()
            }
        } catch (_: Exception) {
            return Result.retry()
        }
    }
}
