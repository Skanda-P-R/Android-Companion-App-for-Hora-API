package com.hora.companion.api

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.hora.companion.data.AuthRepository
import com.hora.companion.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

interface HoraApiService {
    @GET("api/v1/all")
    suspend fun getAllRaw(
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null,
        @Query("location") location: String? = null,
        @Query("date") date: String? = null,
        @Query("time") time: String? = null,
        @Query("lang") lang: String = "en"
    ): ResponseBody

    @GET("api/v1/panchanga")
    suspend fun getPanchanga(
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null,
        @Query("location") location: String? = null,
        @Query("date") date: String? = null,
        @Query("lang") lang: String = "en"
    ): ResponseBody

    @GET("api/v1/hora")
    suspend fun getHora(
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null,
        @Query("location") location: String? = null,
        @Query("date") date: String? = null,
        @Query("time") time: String? = null,
        @Query("lang") lang: String = "en"
    ): ResponseBody

    @GET("api/v1/muhurta")
    suspend fun getMuhurta(
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null,
        @Query("location") location: String? = null,
        @Query("date") date: String? = null,
        @Query("lang") lang: String = "en"
    ): ResponseBody

    @GET("api/v1/kundali")
    suspend fun getKundali(
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null,
        @Query("location") location: String? = null,
        @Query("date") date: String? = null,
        @Query("time") time: String? = null,
        @Query("lang") lang: String = "en"
    ): Map<String, @JvmSuppressWildcards Any>

    @GET("api/v1/kundali/chart")
    suspend fun getKundaliChartRaw(
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null,
        @Query("location") location: String? = null,
        @Query("date") date: String? = null,
        @Query("time") time: String? = null,
        @Query("lang") lang: String = "en"
    ): ResponseBody

    @GET("api/v1/kundali/birth/chart")
    suspend fun getBirthChartRaw(
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null,
        @Query("location") location: String? = null,
        @Query("date") date: String? = null,
        @Query("time") time: String? = null,
        @Query("name") name: String? = null,
        @Query("lang") lang: String = "en"
    ): ResponseBody

    @GET("api/v1/locations")
    suspend fun getLocations(): Map<String, @JvmSuppressWildcards Map<String, @JvmSuppressWildcards Any>>

    @POST("api/v1/locations")
    suspend fun addLocation(@Body location: @JvmSuppressWildcards Map<String, @JvmSuppressWildcards Any?>): ResponseBody

    @DELETE("api/v1/locations/{name}")
    suspend fun deleteLocation(@Path("name") name: String): ResponseBody

    companion object {
        fun create(
            authRepository: AuthRepository,
            onSessionExpired: () -> Unit,
            baseUrl: String = "https://ndaskka.pythonanywhere.com/"
        ): HoraApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(authRepository))
                .addInterceptor(SessionInvalidationInterceptor(onSessionExpired))
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            return retrofit.create(HoraApiService::class.java)
        }
    }
}
