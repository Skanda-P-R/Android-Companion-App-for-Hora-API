package com.hora.companion.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hora.companion.api.AuthService
import com.hora.companion.api.HoraApiService
import com.hora.companion.data.AuthRepository
import com.hora.companion.repository.HoraRepository
import com.hora.companion.ui.login.LoginViewModel

class ViewModelFactory(
    private val context: Context,
    private val authRepository: AuthRepository,
    private val authService: AuthService,
    private val horaRepository: HoraRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(horaRepository) as T
        }
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(authService, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
