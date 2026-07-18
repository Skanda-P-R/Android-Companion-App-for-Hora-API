package com.hora.companion.api

import com.hora.companion.data.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val authRepository: AuthRepository) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        
        // Skip adding Authorization header to the login request itself
        if (original.url.encodedPath.contains("auth/login")) {
            return chain.proceed(original)
        }
        
        val token = runBlocking { authRepository.getSessionTokenBlocking() }
        val builder = original.newBuilder()
        if (token != null) {
            builder.header("Authorization", "Bearer $token")
        }
        
        return chain.proceed(builder.build())
    }
}

class SessionInvalidationInterceptor(
    private val onSessionExpired: () -> Unit
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            onSessionExpired()
        }
        return response
    }
}
