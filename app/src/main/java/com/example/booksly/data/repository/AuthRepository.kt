package com.example.booksly.data.repository

import com.example.booksly.data.remote.AuthApiService
import com.example.booksly.data.remote.dto.AuthDTOs
import com.example.booksly.data.remote.dto.UsuarioResponse
import java.time.LocalDate

class AuthRepository(private val authApiService: AuthApiService) {

    suspend fun loginUser(email: String, password: String): Result<UsuarioResponse> {
        return try {
            val response = authApiService.login(AuthDTOs.LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido del servidor"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerUser(username: String, email: String, password: String, fechaDeNacimiento: LocalDate): Result<UsuarioResponse> {
        return try {
            val request = AuthDTOs.RegisterRequest(username, email, password, fechaDeNacimiento)
            val response = authApiService.register(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido del servidor"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
