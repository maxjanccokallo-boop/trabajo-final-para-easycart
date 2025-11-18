package com.example.easycart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easycart.data.EasyCartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val repo: EasyCartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = repo.login(email, password)

            _uiState.value = if (result.isSuccess) {
                onSuccess()
                AuthUiState()
            } else {
                AuthUiState(error = result.exceptionOrNull?.invoke()?.message ?: "Error de inicio de sesión")
            }
        }
    }

    // ⭐ CORRECCIÓN CRÍTICA: Se actualiza la firma de 'register' para aceptar todos los campos
    fun register(
        fullName: String,
        email: String,
        password: String,
        phone: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            // Llama a la nueva función del repositorio
            val result = repo.register(fullName, email, password, phone)

            _uiState.value = if (result.isSuccess) {
                onSuccess()
                AuthUiState()
            } else {
                AuthUiState(error = result.exceptionOrNull?.invoke()?.message ?: "Error de registro")
            }
        }
    }
}