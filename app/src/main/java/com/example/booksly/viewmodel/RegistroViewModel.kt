package com.example.booksly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksly.data.repository.AuthRepository
import com.example.booksly.data.repository.UsuarioRepository
import com.example.booksly.model.RegistroModel
import com.example.booksly.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException

class RegistroViewModel(
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    private val emailValidator: (String) -> Boolean = { email -> android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() }
) : ViewModel() {

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

    fun onFechaDeNacimientoChange(fecha: String) {
        _uiState.update { it.copy(fechaDeNacimiento = fecha, fechaDeNacimientoError = null) }
    }

    fun onRegistroClick() {
        _uiState.update { it.copy(
            nombreError = null, emailError = null, claveError = null,
            fechaDeNacimientoError = null, mensajeErrorGeneral = null
        )}

        val nombre = _uiState.value.nombre
        val email = _uiState.value.email
        val clave = _uiState.value.clave
        val fechaString = _uiState.value.fechaDeNacimiento
        var hayErrores = false

        if (nombre.isBlank()) {
            _uiState.update { it.copy(nombreError = "El nombre no puede estar vacío") }
            hayErrores = true
        }
        if (email.isBlank() || !emailValidator(email)) {
            _uiState.update { it.copy(emailError = "Correo inválido") }
            hayErrores = true
        }
        if (clave.length < 6) {
            _uiState.update { it.copy(claveError = "La contraseña debe tener al menos 6 caracteres") }
            hayErrores = true
        }

        var fechaDeNacimiento: LocalDate? = null
        try {
            fechaDeNacimiento = LocalDate.parse(fechaString)
        } catch (e: DateTimeParseException) {
            _uiState.update { it.copy(fechaDeNacimientoError = "Formato de fecha inválido (YYYY-MM-DD)") }
            hayErrores = true
        }

        if (hayErrores || fechaDeNacimiento == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            authRepository.registerUser(nombre, email, clave, fechaDeNacimiento!!)
                .onSuccess { usuarioResponse ->
                    val usuarioLocal = Usuario(
                        id = usuarioResponse.id.toInt(),
                        nombre = usuarioResponse.username,
                        email = usuarioResponse.email,
                        clave = "", // No guardamos la clave en la BD local
                        fechaDeNacimiento = usuarioResponse.fechaNacimiento
                    )
                    usuarioRepository.insertUsuario(usuarioLocal)
                    
                    _uiState.update { it.copy(registroExitoso = true, isLoading = false) }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(mensajeErrorGeneral = throwable.message ?: "Error desconocido", isLoading = false) }
                }
        }
    }
}
