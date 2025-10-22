package com.example.booksly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.model.AgregarLibroModel
import com.example.booksly.model.Libro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AgregarLibroViewModel(private val libroRepository: LibroRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AgregarLibroModel())
    val uiState = _uiState.asStateFlow()

    fun onTituloChange(valor: String) {
        _uiState.update { it.copy(titulo = valor, errorTitulo = null) }
    }

    fun onAutorChange(valor: String) {
        _uiState.update { it.copy(autor = valor, errorAutor = null) }
    }

    fun onTotalPaginasChange(valor: String) {
        // Permitir solo números en el campo de páginas
        if (valor.all { it.isDigit() }) {
            _uiState.update { it.copy(totalPaginas = valor, errorTotalPaginas = null) }
        }
    }

    fun onCoverUrlChange(valor: String) {
        _uiState.update { it.copy(coverUrl = valor) }
    }

    fun guardarLibro() {
        _uiState.update { it.copy(
            errorTitulo = null,
            errorAutor = null,
            errorTotalPaginas = null,
            errorGeneral = null
        )}

        val titulo = _uiState.value.titulo
        val autor = _uiState.value.autor
        val paginasStr = _uiState.value.totalPaginas
        val coverUrl = _uiState.value.coverUrl.ifBlank { null } // Si está vacío, usamos null

        var hayErrores = false

        if (titulo.isBlank()) {
            _uiState.update { it.copy(errorTitulo = "El título es obligatorio") }
            hayErrores = true
        }
        if (autor.isBlank()) {
            _uiState.update { it.copy(errorAutor = "El autor es obligatorio") }
            hayErrores = true
        }
        val totalPaginas = paginasStr.toIntOrNull()
        if (totalPaginas == null || totalPaginas <= 0) {
            _uiState.update { it.copy(errorTotalPaginas = "Número de páginas inválido") }
            hayErrores = true
        }

        if (hayErrores || totalPaginas == null) return // Salir si hay errores o paginas es null

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val nuevoLibro = Libro(
                    titulo = titulo,
                    autor = autor,
                    portada = coverUrl ?: "https://placehold.co/120x180/fcf6f8/ff8fa3?text=Sin+Portada",
                    totalPaginas = totalPaginas,
                    paginaActual = 0,
                    estado = "leyendo"
                )
                libroRepository.agregarLibro(nuevoLibro)
                _uiState.update { it.copy(libroGuardado = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorGeneral = "Error al guardar el libro") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}





