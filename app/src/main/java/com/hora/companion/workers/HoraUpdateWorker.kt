package com.hora.companion.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hora.companion.api.HoraApiService
import com.hora.companion.repository.HoraRepository
import com.hora.companion.DataStoreManager
import com.hora.companion.utils.WidgetUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class HoraUpdateWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = coroutineScope {
        val api = HoraApiService.create()
        val repo = HoraRepository(api, applicationContext)
        val dataStoreManager = DataStoreManager(applicationContext)
        
        val location = dataStoreManager.locationFlow.first() ?: (12.9716 to 77.5946)
        val lang = dataStoreManager.langFlow.first()
        
        val panchangaDeferred = async { repo.fetchAllRaw(location.first, location.second, lang) }
        val kundaliDeferred = async { repo.fetchKundaliImage(location.first, location.second, lang) }

        val res = panchangaDeferred.await()
        val kundaliRes = kundaliDeferred.await()

        if (res.isSuccess) {
            WidgetUtils.updateAllWidgets(applicationContext)
            return@coroutineScope Result.success()
        }
        Result.retry()
    }
}
