package com.example.booksly.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.model.AgregarLibroModel
import com.example.booksly.model.Libro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

/**
 * ViewModel para la pantalla de añadir o editar un libro.
 * Gestiona el estado de la UI y la lógica de negocio para crear o actualizar un libro.
 */
class AgregarLibroViewModel(
    private val libroRepository: LibroRepository,
    savedStateHandle: SavedStateHandle,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgregarLibroModel())
    val uiState = _uiState.asStateFlow()

    // Obtiene el ID del libro de los argumentos de navegación. Será nulo si es un libro nuevo.
    private val libroId: Int? = savedStateHandle["libroId"]

    init {
        // Si el libroId no es nulo y es diferente de -1, estamos en modo edición.
        if (libroId != null && libroId != -1) {
            _uiState.update { it.copy(isEditing = true) }
            viewModelScope.launch {
                // Cargamos los datos del libro existente desde el repositorio.
                val libro = libroRepository.obtenerLibroPorId(libroId).first()
                if (libro != null) {
                    // Actualizamos el estado de la UI con los datos del libro.
                    _uiState.update {
                        it.copy(
                            titulo = libro.titulo,
                            autor = libro.autor,
                            totalPaginas = libro.totalPaginas.toString(),
                            portada = libro.portada,
                            libro = libro
                        )
                    }
                }
            }
        }
    }

    // --- Funciones para actualizar el estado desde la UI ---
    fun onTituloChange(valor: String) {
        _uiState.update { it.copy(titulo = valor, errorTitulo = null) }
    }

    fun onAutorChange(valor: String) {
        _uiState.update { it.copy(autor = valor, errorAutor = null) }
    }

    fun onTotalPaginasChange(valor: String) {
        // Solo permite la entrada de dígitos.
        if (valor.all { it.isDigit() }) {
            _uiState.update { it.copy(totalPaginas = valor, errorTotalPaginas = null) }
        }
    }

    /**
     * Se llama cuando el usuario selecciona o cambia la imagen de portada.
     * Guarda la imagen en el almacenamiento interno y actualiza el estado con la ruta.
     */
    fun onPortadaChange(uri: Uri?) {
        if (uri == null) {
            _uiState.update { it.copy(portada = "") }
            return
        }

        viewModelScope.launch {
            val imagePath = saveImageToInternalStorage(uri)
            _uiState.update { it.copy(portada = imagePath ?: "") }
        }
    }

    /**
     * Guarda una imagen desde una URI en el almacenamiento interno de la app
     * Devuelve la ruta absoluta del archivo guardado.
     */
    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = application.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val directory = File(application.cacheDir, "portadas")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, "${UUID.randomUUID()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()
            inputStream?.close()
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Valida los campos y guarda o actualiza el libro en la base de datos.
     */
    fun guardarLibro() {
        // --- Validaciones (simplificadas) ---
        val totalPaginas = _uiState.value.totalPaginas.toIntOrNull()
        if (totalPaginas == null) { /* ...manejar error... */ return }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val libroActual = _uiState.value.libro
                val nuevoLibro = Libro(
                    id = if (_uiState.value.isEditing) libroId!! else 0,
                    titulo = _uiState.value.titulo,
                    autor = _uiState.value.autor,
                    portada = _uiState.value.portada,
                    totalPaginas = totalPaginas,
                    // Mantiene los valores existentes si se está editando.
                    paginaActual = if (_uiState.value.isEditing) libroActual?.paginaActual ?: 0 else 0,
                    estado = if (_uiState.value.isEditing) libroActual?.estado ?: "leyendo" else "leyendo"
                )

                // Llama al método correspondiente del repositorio.
                if (_uiState.value.isEditing) {
                    libroRepository.actualizarLibro(nuevoLibro)
                } else {
                    libroRepository.agregarLibro(nuevoLibro)
                }

                _uiState.update { it.copy(libroGuardado = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorGeneral = "Error al guardar el libro") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}