package com.example.booksly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksly.data.remote.dto.UsuarioResponse
import com.example.booksly.data.repository.AuthRepository
import com.example.booksly.data.repository.PreferenciasRepository
import com.example.booksly.data.repository.UsuarioRepository
import com.example.booksly.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class Success(val user: UsuarioResponse) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    private val preferenciasRepository: PreferenciasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    fun onEmailChange(email: String) { _email.value = email }
    fun onPasswordChange(password: String) { _password.value = password }

    fun onLoginClicked() {
        val email = _email.value
        val pass = _password.value

        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = LoginUiState.Error("Email y contraseña no pueden estar vacíos")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            authRepository.loginUser(email, pass)
                .onSuccess { usuarioResponse ->
                    val usuarioLocal = Usuario(
                        id = usuarioResponse.id.toInt(),
                        nombre = usuarioResponse.username,
                        email = usuarioResponse.email,
                        clave = "", // No guardamos la clave en la BD local
                        fechaDeNacimiento = usuarioResponse.fechaNacimiento
                    )
                    usuarioRepository.insertUsuario(usuarioLocal)

                    preferenciasRepository.guardarUsuarioEmail(usuarioResponse.email)
                    
                    _uiState.value = LoginUiState.Success(usuarioResponse)
                }
                .onFailure { throwable ->
                    _uiState.value = LoginUiState.Error(throwable.message ?: "Error desconocido")
                }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
