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

/**
 * ViewModel para la pantalla de registro.
 * Gestiona el estado de la UI y la lógica de negocio para el registro de usuarios.
 *
 * @param usuarioRepository Repositorio para interactuar con los datos de los usuarios.
 */
class RegistroViewModel(private val usuarioRepository: UsuarioRepository) : ViewModel() {

    // Flujo de estado mutable y privado para el estado de la UI.
    private val _uiState = MutableStateFlow(RegistroModel())
    // Flujo de estado público e inmutable para que la UI lo observe.
    val uiState = _uiState.asStateFlow()

    /**
     * Actualiza el nombre en el estado de la UI y limpia cualquier error asociado.
     */
    fun onNombreChange(nombre: String) {
        _uiState.update { it.copy(nombre = nombre, nombreError = null) }
    }

    /**
     * Actualiza el email en el estado de la UI y limpia cualquier error asociado.
     */
    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    /**
     * Actualiza la contraseña en el estado de la UI y limpia cualquier error asociado.
     */
    fun onClaveChange(clave: String) {
        _uiState.update { it.copy(clave = clave, claveError = null) }
    }

    /**
     * Se ejecuta cuando el usuario hace clic en el botón de registro.
     * Valida los campos y, si son correctos, intenta crear un nuevo usuario.
     */
    fun onRegistroClick() {
        // Primero, limpiamos los errores previos.
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

        // --- Validación de campos ---
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

        // Si hay errores de validación, no continuamos.
        if (hayErrores) return

        // Lanzamos una corrutina para realizar la operación de registro en segundo plano.
        viewModelScope.launch {
            // Activamos el estado de carga.
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Verificamos si el usuario ya existe en la base de datos.
                if (usuarioRepository.buscarUsuarioPorEmail(email) != null) {
                    _uiState.update { it.copy(mensajeErrorGeneral = "El correo electrónico ya está registrado") }
                } else {
                    // Si no existe, creamos el nuevo usuario y lo guardamos.
                    val nuevoUsuario = Usuario(nombre = nombre, email = email, clave = clave)
                    usuarioRepository.crearUsuario(nuevoUsuario)
                    // Actualizamos el estado para indicar que el registro fue exitoso.
                    _uiState.update { it.copy(registroExitoso = true) }
                }
            } catch (e: Exception) {
                // En caso de un error inesperado, mostramos un mensaje general.
                _uiState.update { it.copy(mensajeErrorGeneral = "Ocurrió un error inesperado") }
            } finally {
                // Desactivamos el estado de carga, tanto si hubo éxito como si hubo error.
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}