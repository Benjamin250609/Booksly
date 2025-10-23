package com.example.booksly.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.model.LibroDetalleModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibroDetalleViewModel(
    private val libroRepository: LibroRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {


    private val libroId: Int = checkNotNull(savedStateHandle["libroId"])

    private val _uiState = MutableStateFlow(LibroDetalleModel())
    val uiState: StateFlow<LibroDetalleModel> = _uiState.asStateFlow()

    init {

        viewModelScope.launch {
            libroRepository.obtenerLibroPorId(libroId)

                .catch { exception ->
                    _uiState.update { it.copy(isLoading = false, errorCarga = "Error al cargar el libro") }
                }
                .collect { libroDb ->
                    if (libroDb != null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                libro = libroDb,
                                paginaActualInput = libroDb.paginaActual.toString()
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorCarga = "Libro no encontrado") }
                    }
                }
        }
    }


    fun onPaginaActualInputChange(valor: String) {
        if (valor.all { it.isDigit() }) {
            _uiState.update { it.copy(paginaActualInput = valor, errorPaginaInput = null, progresoGuardado = false) }
        }
    }

    fun guardarProgresoPagina() {
        val libroActual = _uiState.value.libro ?: return
        val nuevaPaginaStr = _uiState.value.paginaActualInput
        val nuevaPagina = nuevaPaginaStr.toIntOrNull()

        // Validar la entrada
        if (nuevaPagina == null || nuevaPagina < 0 || nuevaPagina > libroActual.totalPaginas) {
            _uiState.update { it.copy(errorPaginaInput = "Página inválida (0-${libroActual.totalPaginas})") }
            return
        }

        viewModelScope.launch {
            try {
                // Crear objeto actualizado
                val libroActualizado = libroActual.copy(
                    paginaActual = nuevaPagina,
                    // Marcar como finalizado si llega a la última página
                    estado = if (nuevaPagina == libroActual.totalPaginas) "finalizado" else "leyendo"
                )
                libroRepository.actualizarLibro(libroActualizado)
                _uiState.update { it.copy(progresoGuardado = true, errorPaginaInput = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorPaginaInput = "Error al guardar") }
            }
        }
    }

    fun marcarComoTerminado() {
        val libroActual = _uiState.value.libro ?: return

        // Solo actualiza si no está ya terminado
        if (libroActual.estado != "finalizado" || libroActual.paginaActual != libroActual.totalPaginas) {
            viewModelScope.launch {
                try {
                    val libroActualizado = libroActual.copy(
                        paginaActual = libroActual.totalPaginas,
                        estado = "finalizado"
                    )
                    libroRepository.actualizarLibro(libroActualizado)
                    _uiState.update { it.copy(progresoGuardado = true, paginaActualInput = libroActualizado.paginaActual.toString()) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(errorPaginaInput = "Error al finalizar") }
                }
            }
        }
    }
}
