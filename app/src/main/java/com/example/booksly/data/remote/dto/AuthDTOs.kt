package com.example.booksly.data.remote.dto

import java.time.LocalDate

object AuthDTOs {
    data class RegisterRequest(
        val username: String,
        val email: String,
        val password: String,
        val fechaNacimiento: LocalDate
    )

    data class LoginRequest(
        val email: String,
        val password: String
    )
}
