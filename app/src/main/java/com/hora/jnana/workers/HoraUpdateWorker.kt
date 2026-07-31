package com.hora.jnana.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hora.jnana.api.HoraApiService
import com.hora.jnana.repository.HoraRepository
import com.hora.jnana.DataStoreManager
import com.hora.jnana.data.AuthRepository
import com.hora.jnana.utils.WidgetUtils
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
            val horaRes = repo.fetchHora(lat, lon, locationName, lang = lang, force = false)
            
            return if (horaRes.isSuccess) {
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
