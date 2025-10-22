package com.example.booksly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksly.data.repository.UsuarioRepository
import com.example.booksly.model.RegistroModel
import com.example.booksly.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegistroViewModel(private val usuarioRepository: UsuarioRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroModel())
    val uiState = _uiState.asStateFlow()

    fun onNombreChange(nombre: String) {
        _uiState.update { it.copy(nombre = nombre, nombreError = null) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onClaveChange(clave: String) {
        _uiState.update { it.copy(clave = clave, claveError = null) }
    }

    fun onRegistroClick() {
        _uiState.update { it.copy(
            nombreError = null,
            emailError = null,
            claveError = null,
            mensajeErrorGeneral = null
        )}

        val nombre = _uiState.value.nombre
        val email = _uiState.value.email
        val clave = _uiState.value.clave
        var hayErrores = false

        if (nombre.isBlank()) {
            _uiState.update { it.copy(nombreError = "El nombre no puede estar vacío") }
            hayErrores = true
        }

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(emailError = "Correo inválido") }
            hayErrores = true
        }

        if (clave.length < 6) {
            _uiState.update { it.copy(claveError = "La contraseña debe tener al menos 6 caracteres") }
            hayErrores = true
        }

        if (hayErrores) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Verificar si el usuario ya existe
                if (usuarioRepository.buscarUsuarioPorEmail(email) != null) {
                    _uiState.update { it.copy(mensajeErrorGeneral = "El correo electrónico ya está registrado") }
                } else {

                    val nuevoUsuario = Usuario(nombre = nombre, correo = email, contrasena = clave)
                    usuarioRepository.crearUsuario(nuevoUsuario)
                    _uiState.update { it.copy(registroExitoso = true) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(mensajeErrorGeneral = "Ocurrió un error inesperado") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}