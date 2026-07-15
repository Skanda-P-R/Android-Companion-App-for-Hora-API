package com.hora.companion.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

interface HoraApiService {
    @GET("/api/v1/all")
    suspend fun getAllRaw(@Query("lat") lat: Double, @Query("lon") lon: Double): ResponseBody

    @GET("/api/v1/kundali")
    suspend fun getKundali(@Query("lat") lat: Double, @Query("lon") lon: Double): Map<String, Any>

    @GET("/api/v1/kundali/chart")
    suspend fun getKundaliChartRaw(
        @Query("lat") lat: Double, 
        @Query("lon") lon: Double,
        @Query("lang") lang: String = "en"
    ): ResponseBody

    companion object {
        fun create(baseUrl: String = "https://dannyboiii.pythonanywhere.com/"): HoraApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
            return retrofit.create(HoraApiService::class.java)
        }
    }
}
