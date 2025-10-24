package com.example.booksly.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.data.repository.NotaRepository
import com.example.booksly.model.LibroDetalleModel
import com.example.booksly.model.Nota
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibroDetalleViewModel(
    private val libroRepository: LibroRepository,
    private val notaRepository: NotaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val libroId: Int = checkNotNull(savedStateHandle["libroId"])

    private val _uiState = MutableStateFlow(LibroDetalleModel())
    val uiState: StateFlow<LibroDetalleModel> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                libroRepository.obtenerLibroPorId(libroId),
                notaRepository.obtenerNotasPorLibro(libroId)
            ) { libro, notas ->
                _uiState.update {
                    it.copy(
                        libro = libro,
                        notas = notas,
                        isLoading = false,
                        paginaActualInput = libro?.paginaActual?.toString() ?: ""
                    )
                }
            }.catch { exception ->
                _uiState.update { it.copy(isLoading = false, errorCarga = "Error al cargar el libro y sus notas.") }
            }.collect {}
        }
    }

    // --- Lógica de Progreso ---
    fun onPaginaActualInputChange(valor: String) {
        if (valor.all { it.isDigit() }) {
            _uiState.update { it.copy(paginaActualInput = valor, errorPaginaInput = null, progresoGuardado = false) }
        }
    }

    fun guardarProgresoPagina() {
        val libroActual = _uiState.value.libro ?: return
        val nuevaPagina = _uiState.value.paginaActualInput.toIntOrNull()

        if (nuevaPagina == null || nuevaPagina < 0 || nuevaPagina > libroActual.totalPaginas) {
            _uiState.update { it.copy(errorPaginaInput = "Página inválida (0-${libroActual.totalPaginas})") }
            return
        }

        viewModelScope.launch {
            val libroActualizado = libroActual.copy(
                paginaActual = nuevaPagina,
                estado = if (nuevaPagina == libroActual.totalPaginas) "finalizado" else "leyendo"
            )
            libroRepository.actualizarLibro(libroActualizado)
            _uiState.update { it.copy(progresoGuardado = true, errorPaginaInput = null) }
        }
    }

    // --- Lógica de Diario ---
    fun onNuevaNotaChange(texto: String) {
        _uiState.update { it.copy(nuevaNotaTexto = texto) }
    }

    fun guardarNuevaNota() {
        val textoNota = _uiState.value.nuevaNotaTexto.trim()
        if (textoNota.isBlank()) return

        viewModelScope.launch {
            val nuevaNota = Nota(libroId = libroId, texto = textoNota)
            notaRepository.insertarNota(nuevaNota)
            _uiState.update { it.copy(nuevaNotaTexto = "") } // Limpia el campo de texto
        }
    }

    fun eliminarNota(nota: Nota) {
        viewModelScope.launch {
            notaRepository.eliminarNota(nota)
        }
    }

    // --- Lógica de Eliminación de Libro ---
    fun onEliminarClick() {
        _uiState.update { it.copy(showConfirmacionEliminar = true) }
    }

    fun onDismissEliminar() {
        _uiState.update { it.copy(showConfirmacionEliminar = false) }
    }

    fun onConfirmarEliminar() {
        _uiState.value.libro?.let {
            viewModelScope.launch {
                libroRepository.eliminarLibro(it)
                _uiState.update { it.copy(libroEliminado = true, showConfirmacionEliminar = false) }
            }
        }
    }
}
