package com.example.booksly.model

/**
 * Data class que representa el estado de la UI para la pantalla de añadir o editar un libro.
 */
data class AgregarLibroModel(
    // --- Campos del Formulario ---
    val titulo: String = "",
    val autor: String = "",
    val totalPaginas: String = "", // Se usa String para facilitar la entrada en el TextField, luego se convierte a Int.
    val portada: String = "", // URL de la imagen de portada.

    // --- Errores de Validación ---
    val errorTitulo: String? = null,
    val errorAutor: String? = null,
    val errorTotalPaginas: String? = null,

    // --- Estado de la UI ---
    val libroGuardado: Boolean = false, // Se activa a true cuando el libro se ha guardado con éxito.
    val isLoading: Boolean = false, // Indica si se está ejecutando una operación de guardado.
    val errorGeneral: String? = null, // Para mostrar un mensaje de error no asociado a un campo específico.

    // --- Modo Edición ---
    // Indica si la pantalla está en modo de edición (true) o creación (false).
    val isEditing: Boolean = false,
    // Almacena los datos del libro original cuando se está en modo de edición.
    val libro: Libro? = null
)
