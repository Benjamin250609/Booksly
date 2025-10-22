package com.example.booksly.model


data class LoginModel(
    val email: String = "",
    val contrasena: String = "",
    val emailError: String? = null,
    val contrasenaError: String? = null,
    val loginExitoso: Boolean = false,
    val mensajeErrorGeneral: String? = null,
    val isLoading: Boolean = false
)
