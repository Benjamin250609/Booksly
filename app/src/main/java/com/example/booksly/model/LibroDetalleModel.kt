package com.example.booksly.model

data class LibroDetalleModel(
    val libro: Libro? = null,
    val paginaActualInput: String = "",
    val errorPaginaInput: String? = null,
    val isLoading: Boolean = true,
    val errorCarga: String? = null,
    val progresoGuardado: Boolean = false
)