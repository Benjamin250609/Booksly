package com.example.booksly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksly.data.repository.UsuarioRepository
import com.example.booksly.model.LoginModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class LoginViewModel(private val usuarioRepository: UsuarioRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginModel())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onContrasenaChange(contrasena: String) {
        _uiState.update { it.copy(contrasena = contrasena, contrasenaError = null) }
    }

    fun onLoginClick() {
        // Limpiar errores previos
        _uiState.update { it.copy(
            emailError = null,
            contrasenaError = null,
            mensajeErrorGeneral = null
        )}

        val email = _uiState.value.email
        val contrasena = _uiState.value.contrasena
        var hayErrores = false

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(emailError = "Correo inválido") }
            hayErrores = true
        }

        if (contrasena.isBlank()) {
            _uiState.update { it.copy(contrasenaError = "La contraseña no puede estar vacía") }
            hayErrores = true
        }

        if (hayErrores) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val usuario = usuarioRepository.buscarUsuarioPorEmail(email)
                if (usuario == null) {
                    _uiState.update { it.copy(mensajeErrorGeneral = "Usuario no encontrado") }
                } else if (usuario.contrasena != contrasena) {
                    _uiState.update { it.copy(mensajeErrorGeneral = "Contraseña incorrecta") }
                } else {
                    _uiState.update { it.copy(loginExitoso = true) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(mensajeErrorGeneral = "Ocurrió un error inesperado") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
