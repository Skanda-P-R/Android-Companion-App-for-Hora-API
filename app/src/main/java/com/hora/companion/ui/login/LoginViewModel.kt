package com.hora.companion.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hora.companion.BuildConfig
import com.hora.companion.data.AuthRepository
import com.hora.companion.api.AuthService
import com.hora.companion.api.models.LoginRequest
import com.hora.companion.api.models.ApiErrorResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LoginViewModel(
    private val authService: AuthService,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val TAG = "LoginViewModel"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val errorAdapter = moshi.adapter(ApiErrorResponse::class.java)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(username: String, uuid: String, onLoginSuccess: () -> Unit) {
        if (username.isBlank()) {
            _uiState.value = LoginUiState.Error("Username cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val response = authService.login(LoginRequest(username, uuid))
                authRepository.saveSessionToken(response.token)
                _uiState.value = LoginUiState.Success
                onLoginSuccess()
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val apiError = try {
                    errorBody?.let { errorAdapter.fromJson(it) }?.error
                } catch (parseException: Exception) {
                    null
                }

                val errorMsg = apiError?.message ?: when (e.code()) {
                    404 -> "Username not registered on server."
                    403 -> "This username is registered on another device."
                    else -> "Authentication failed: ${e.message()}"
                }
                _uiState.value = LoginUiState.Error(errorMsg)
            } catch (e: IOException) {
                if (BuildConfig.DEBUG) Log.e(TAG, "Network error", e)
                _uiState.value = LoginUiState.Error("Network error. Check connection.")
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e(TAG, "Unexpected error during login", e)
                _uiState.value = LoginUiState.Error("Unexpected error: ${e.localizedMessage ?: "Unknown"}")
            }
        }
    }
}
