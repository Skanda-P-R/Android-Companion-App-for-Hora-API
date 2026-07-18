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
                // In background, we just notify repo, which might not be ideal for UI but good for state
                authRepository.notifySessionExpired() 
            }
        )
        val repo = HoraRepository(api, applicationContext)
        
        val location = dataStoreManager.locationFlow.first()
        val locationName = dataStoreManager.locationNameFlow.first()
        val locationMode = dataStoreManager.locationModeFlow.first()
        val lang = dataStoreManager.langFlow.first()
        
        val res = if (locationMode == "gps" && location != null) {
            val panchangaDeferred = async { repo.fetchAllRaw(lat = location.first, lon = location.second, lang = lang) }
            val kundaliDeferred = async { repo.fetchKundaliImage(lat = location.first, lon = location.second, lang = lang) }
            kundaliDeferred.await()
            panchangaDeferred.await()
        } else {
            val panchangaDeferred = async { repo.fetchAllRaw(location = locationName, lang = lang) }
            val kundaliDeferred = async { repo.fetchKundaliImage(location = locationName, lang = lang) }
            kundaliDeferred.await()
            panchangaDeferred.await()
        }

        if (res.isSuccess) {
            WidgetUtils.updateAllWidgets(applicationContext)
            return@coroutineScope Result.success()
        }
        Result.retry()
    }
}
