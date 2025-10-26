package com.example.booksly.model

// Data class que representa el estado de la pantalla de inicio de sesión.
data class LoginModel(
    // Credenciales introducidas por el usuario.
    val email: String = "",
    val contrasena: String = "",
    // Mensajes de error para los campos del formulario.
    val emailError: String? = null,
    val contrasenaError: String? = null,
    // Banderas para controlar el flujo de la UI durante el login.
    val loginExitoso: Boolean = false,
    val mensajeErrorGeneral: String? = null,
    val isLoading: Boolean = false
)
