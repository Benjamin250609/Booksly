package com.example.booksly.model

data class RegistroModel(
    val nombre: String = "",
    val email: String = "",
    val clave: String = "",
    val nombreError: String? = null,
    val emailError: String? = null,
    val claveError: String? = null,
    val registroExitoso: Boolean = false,
    val mensajeErrorGeneral: String? = null,
    val isLoading: Boolean = false
)
