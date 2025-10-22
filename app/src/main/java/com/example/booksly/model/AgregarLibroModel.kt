package com.example.booksly.model

data class AgregarLibroModel(
    val titulo: String = "",
    val autor: String = "",
    val totalPaginas: String = "", // Usamos String para el input, luego convertimos
    val coverUrl: String = "",
    val errorTitulo: String? = null,
    val errorAutor: String? = null,
    val errorTotalPaginas: String? = null,
    val libroGuardado: Boolean = false,
    val isLoading: Boolean = false,
    val errorGeneral: String? = null
)
