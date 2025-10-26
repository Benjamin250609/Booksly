package com.example.booksly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksly.data.repository.PreferenciasRepository
import com.example.booksly.data.repository.UsuarioRepository
import com.example.booksly.model.LoginModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de inicio de sesión.
 * Gestiona el estado de la UI y la lógica de negocio para la autenticación de usuarios.
 *
 * @param usuarioRepository Repositorio para acceder a los datos de los usuarios.
 * @param preferenciasRepository Repositorio para guardar el estado de la sesión del usuario.
 */
class LoginViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val preferenciasRepository: PreferenciasRepository
) : ViewModel() {

    // Flujo de estado mutable y privado para el estado de la UI.
    private val _uiState = MutableStateFlow(LoginModel())
    // Flujo de estado público e inmutable para que la UI lo observe.
    val uiState = _uiState.asStateFlow()

    /**
     * Actualiza el email en el estado de la UI y limpia cualquier error asociado.
     */
    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    /**
     * Actualiza la contraseña en el estado de la UI y limpia cualquier error asociado.
     */
    fun onContrasenaChange(contrasena: String) {
        _uiState.update { it.copy(contrasena = contrasena, contrasenaError = null) }
    }

    /**
     * Se ejecuta cuando el usuario hace clic en el botón de inicio de sesión.
     * Valida los campos y, si son correctos, intenta autenticar al usuario.
     */
    fun onLoginClick() {
        // Limpiamos los errores previos antes de una nueva validación.
        _uiState.update { it.copy(
            emailError = null,
            contrasenaError = null,
            mensajeErrorGeneral = null
        )}

        val email = _uiState.value.email
        val contrasena = _uiState.value.contrasena
        var hayErrores = false

        // --- Validación de campos ---
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(emailError = "Correo inválido") }
            hayErrores = true
        }

        if (contrasena.isBlank()) {
            _uiState.update { it.copy(contrasenaError = "La contraseña no puede estar vacía") }
            hayErrores = true
        }

        if (hayErrores) return

        // Lanzamos una corrutina para realizar la autenticación en segundo plano.
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val usuario = usuarioRepository.buscarUsuarioPorEmail(email)
                when {
                    usuario == null -> {
                        _uiState.update { it.copy(mensajeErrorGeneral = "Usuario no encontrado") }
                    }
                    usuario.clave != contrasena -> {
                        _uiState.update { it.copy(mensajeErrorGeneral = "Contraseña incorrecta") }
                    }
                    else -> {
                        // --- Inicio de sesión exitoso ---
                        // Guardamos el email del usuario para mantener la sesión iniciada.
                        preferenciasRepository.guardarUsuarioEmail(email)
                        // Actualizamos el estado para notificar a la UI que el login fue exitoso.
                        _uiState.update { it.copy(loginExitoso = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(mensajeErrorGeneral = "Ocurrió un error inesperado") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
