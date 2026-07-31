package com.hora.jnana.ui.login

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.hora.jnana.BuildConfig
import com.hora.jnana.api.AuthService
import com.hora.jnana.api.models.ApiErrorResponse
import com.hora.jnana.api.models.GoogleLoginRequest
import com.hora.jnana.data.AuthRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LoginViewModel(
    private val authService: AuthService,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val tag = "LoginViewModel"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val errorAdapter = moshi.adapter(ApiErrorResponse::class.java)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun loginWithGoogle(context: Context, uuid: String, onLoginSuccess: () -> Unit) {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts = false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setAutoSelectEnabled(true)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )
                
                val credential = result.credential
                when {
                    credential is GoogleIdTokenCredential -> {
                        performBackendLogin(credential.idToken, uuid, onLoginSuccess)
                    }
                    credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                        try {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            performBackendLogin(googleIdTokenCredential.idToken, uuid, onLoginSuccess)
                        } catch (e: GoogleIdTokenParsingException) {
                            if (BuildConfig.DEBUG) Log.e(tag, "Google ID Token parsing error", e)
                            _uiState.value = LoginUiState.Error("Failed to parse Google account info")
                        }
                    }
                    else -> {
                        if (BuildConfig.DEBUG) Log.e(tag, "Unexpected credential type: ${credential.type}")
                        _uiState.value = LoginUiState.Error("Unexpected sign-in response")
                    }
                }
            } catch (e: GetCredentialException) {
                if (BuildConfig.DEBUG) Log.e(tag, "Credential Manager error", e)
                _uiState.value = LoginUiState.Error("Sign-in cancelled or failed")
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e(tag, "Google sign-in error", e)
                _uiState.value = LoginUiState.Error("An error occurred during sign-in")
            }
        }
    }

    private suspend fun performBackendLogin(idToken: String, uuid: String, onLoginSuccess: () -> Unit) {
        try {
            val response = authService.googleLogin(GoogleLoginRequest(idToken, uuid))
            authRepository.saveSessionToken(response.token)
            response.user?.let {
                authRepository.saveUserInfo(it.email, it.name, it.picture)
            }
            _uiState.value = LoginUiState.Success
            onLoginSuccess()
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val apiError = try {
                errorBody?.let { errorAdapter.fromJson(it) }?.error
            } catch (_: Exception) {
                null
            }

            val errorMsg = apiError?.message ?: when (e.code()) {
                403 -> "Email address not authorized. Contact administrator."
                401 -> "Invalid Google session. Please try again."
                else -> "Authentication failed: ${e.message()}"
            }
            _uiState.value = LoginUiState.Error(errorMsg)
        } catch (e: IOException) {
            if (BuildConfig.DEBUG) Log.e(tag, "Network error", e)
            _uiState.value = LoginUiState.Error("Network error. Check connection.")
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(tag, "Unexpected error during login", e)
            _uiState.value = LoginUiState.Error("Unexpected error: ${e.localizedMessage ?: "Unknown"}")
        }
    }
}
