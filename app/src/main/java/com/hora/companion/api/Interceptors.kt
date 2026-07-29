package com.hora.companion.api

import com.hora.companion.data.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val repository: AuthRepository) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        
        if (original.url.encodedPath.contains("auth/login")) {
            return chain.proceed(original)
        }
        
        val token = runBlocking { repository.getSessionTokenBlocking() }
        val builder = original.newBuilder()
        if (token != null) {
            builder.header("Authorization", "Bearer $token")
        }
        
        return chain.proceed(builder.build())
    }
}

class SessionInvalidationInterceptor(
    private val onExpired: () -> Unit
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            onExpired()
        }
        return response
    }
}
