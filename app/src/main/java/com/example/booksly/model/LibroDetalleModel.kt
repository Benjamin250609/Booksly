package com.example.booksly.model

data class LibroDetalleModel(
    val libro: Libro? = null,
    val paginaActualInput: String = "",
    val errorPaginaInput: String? = null,
    val isLoading: Boolean = true,
    val errorCarga: String? = null,
    val progresoGuardado: Boolean = false,
    val showConfirmacionEliminar: Boolean = false,
    val libroEliminado: Boolean = false,
    // --- AÑADIDO PARA EL DIARIO ---
    val notas: List<Nota> = emptyList(),
    val nuevaNotaTexto: String = ""
)
