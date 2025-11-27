package com.example.booksly.model

/**
 * Modelo de datos que representa el estado de la UI de la pantalla de registro.
 */
data class RegistroModel(
    val nombre: String = "",
    val email: String = "",
    val clave: String = "",
    val fechaDeNacimiento: String = "", // Campo añadido
    val nombreError: String? = null,
    val emailError: String? = null,
    val claveError: String? = null,
    val fechaDeNacimientoError: String? = null, // Error para el nuevo campo
    val mensajeErrorGeneral: String? = null,
    val isLoading: Boolean = false,
    val registroExitoso: Boolean = false
)
