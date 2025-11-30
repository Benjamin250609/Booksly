package com.example.booksly.data.remote.dto

import java.time.LocalDate

data class UsuarioResponse(
    val id: Long,
    val username: String,
    val email: String,
    val fechaNacimiento: LocalDate
)
