package com.example.booksly.model

/**
 * Data class que representa el estado de la UI para la pantalla de detalle de un libro.
 */
data class LibroDetalleModel(
    // --- Datos del Libro ---
    val libro: Libro? = null, // El objeto libro que se está mostrando. Es nulo inicialmente hasta que se carga.

    // --- Gestión del Progreso ---
    val paginaActualInput: String = "", // El valor del campo de texto para actualizar la página actual.
    val errorPaginaInput: String? = null, // Mensaje de error para la validación de la página introducida.

    // --- Estado de la UI ---
    val isLoading: Boolean = true, // Indica si se están cargando los datos del libro.
    val errorCarga: String? = null, // Mensaje de error si falla la carga del libro.
    val progresoGuardado: Boolean = false, // Se pone a true brevemente después de guardar el progreso.
    val showConfirmacionEliminar: Boolean = false, // Controla la visibilidad del diálogo de confirmación para eliminar el libro.
    val libroEliminado: Boolean = false, // Se pone a true cuando el libro ha sido eliminado para disparar la navegación.

    // --- Gestión de Notas/Diario ---
    val notas: List<Nota> = emptyList(), // La lista de notas asociadas a este libro.
    val nuevaNotaTexto: String = "" // El texto de la nueva nota que está escribiendo el usuario.
)
