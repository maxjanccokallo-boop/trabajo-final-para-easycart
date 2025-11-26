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
        _uiState.value = AuthUiState(isLoading = true)

        repo.login(email, password) { result ->
            if (result.isSuccess) {
                _uiState.value = AuthUiState()
                onSuccess()
            } else {
                _uiState.value = AuthUiState(
                    error = result.exceptionOrNull?.invoke()?.message
                        ?: "Error de inicio de sesión"
                )
            }
        }
    }

    fun register(
        fullName: String,
        email: String,
        password: String,
        phone: String,
        onSuccess: () -> Unit
    ) {
        _uiState.value = AuthUiState(isLoading = true)

        repo.register(fullName, email, password, phone) { result ->
            if (result.isSuccess) {
                _uiState.value = AuthUiState()
                onSuccess()
            } else {
                _uiState.value = AuthUiState(
                    error = result.exceptionOrNull?.invoke()?.message
                        ?: "Error de registro"
                )
            }
        }
    }
}