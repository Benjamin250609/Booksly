package com.example.booksly.data.remote

import com.example.booksly.data.remote.dto.AuthDTOs
import com.example.booksly.data.remote.dto.UsuarioResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/register")
    suspend fun register(@Body request: AuthDTOs.RegisterRequest): Response<UsuarioResponse>

    @POST("auth/login")
    suspend fun login(@Body request: AuthDTOs.LoginRequest): Response<UsuarioResponse>
}
