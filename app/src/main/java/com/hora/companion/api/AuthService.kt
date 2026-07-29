package com.hora.companion.api

import com.hora.companion.BuildConfig
import com.hora.companion.api.models.GoogleLoginRequest
import com.hora.companion.api.models.LoginResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/v1/auth/google-login")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): LoginResponse

    companion object {
        fun create(
            baseUrl: String = BuildConfig.BASE_URL,
            client: OkHttpClient? = null
        ): AuthService {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val builder = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
            
            if (client != null) {
                builder.client(client)
            }

            return builder.build().create(AuthService::class.java)
        }
    }
}
