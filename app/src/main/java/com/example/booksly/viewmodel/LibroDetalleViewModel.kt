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

/**
 * ViewModel para la pantalla de detalle de un libro.
 * Gestiona el estado de la UI y la lógica para mostrar los detalles de un libro,
 * sus notas, actualizar el progreso de lectura y eliminar el libro o sus notas.
 */
class LibroDetalleViewModel(
    private val libroRepository: LibroRepository,
    private val notaRepository: NotaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Obtiene el ID del libro de los argumentos de navegación. Lanza una excepción si es nulo.
    private val libroId: Int = checkNotNull(savedStateHandle["libroId"])

    private val _uiState = MutableStateFlow(LibroDetalleModel())
    val uiState: StateFlow<LibroDetalleModel> = _uiState.asStateFlow()

    init {
        // En la inicialización, se combinan dos flujos: uno para el libro y otro para las notas.
        viewModelScope.launch {
            combine(
                libroRepository.obtenerLibroPorId(libroId), // Flujo que emite el libro.
                notaRepository.obtenerNotasPorLibro(libroId) // Flujo que emite la lista de notas.
            ) { libro, notas ->
                // Cuando cualquiera de los flujos emite un nuevo valor, se actualiza el estado de la UI.
                _uiState.update {
                    it.copy(
                        libro = libro,
                        notas = notas,
                        isLoading = false,
                        paginaActualInput = libro?.paginaActual?.toString() ?: ""
                    )
                }
            }.catch { exception ->
                // Si ocurre un error en alguno de los flujos, se actualiza el estado con un error.
                _uiState.update { it.copy(isLoading = false, errorCarga = "Error al cargar el libro y sus notas.") }
            }.collect{} // Se inicia la recolección de los flujos.
        }
    }

    // --- Lógica de Progreso ---
    fun onPaginaActualInputChange(valor: String) {
        // Permite solo dígitos en el campo de texto de la página actual.
        if (valor.all { it.isDigit() }) {
            _uiState.update { it.copy(paginaActualInput = valor, errorPaginaInput = null, progresoGuardado = false) }
        }
    }

    fun guardarProgresoPagina() {
        val libroActual = _uiState.value.libro ?: return
        val nuevaPagina = _uiState.value.paginaActualInput.toIntOrNull()

        // Validación de la página introducida.
        if (nuevaPagina == null || nuevaPagina < 0 || nuevaPagina > libroActual.totalPaginas) {
            _uiState.update { it.copy(errorPaginaInput = "Página inválida (0-${libroActual.totalPaginas})") }
            return
        }

        // Actualiza el libro en la base de datos.
        viewModelScope.launch {
            val libroActualizado = libroActual.copy(
                paginaActual = nuevaPagina,
                // Si la página actual es la última, el estado cambia a "finalizado".
                estado = if (nuevaPagina == libroActual.totalPaginas) "leído" else "leyendo"
            )
            libroRepository.actualizarLibro(libroActualizado)
            _uiState.update { it.copy(progresoGuardado = true, errorPaginaInput = null) }
        }
    }

    // --- Lógica de Diario/Notas ---
    fun onNuevaNotaChange(texto: String) {
        _uiState.update { it.copy(nuevaNotaTexto = texto) }
    }

    fun guardarNuevaNota() {
        val textoNota = _uiState.value.nuevaNotaTexto.trim()
        if (textoNota.isBlank()) return

        viewModelScope.launch {
            val nuevaNota = Nota(libroId = libroId, texto = textoNota)
            notaRepository.insertarNota(nuevaNota)
            _uiState.update { it.copy(nuevaNotaTexto = "") } // Limpia el campo de texto tras guardar.
        }
    }

    fun eliminarNota(nota: Nota) {
        viewModelScope.launch {
            notaRepository.eliminarNota(nota)
        }
    }

    // --- Lógica de Eliminación de Libro ---
    fun onEliminarClick() {
        // Muestra el diálogo de confirmación.
        _uiState.update { it.copy(showConfirmacionEliminar = true) }
    }

    fun onDismissEliminar() {
        // Oculta el diálogo de confirmación.
        _uiState.update { it.copy(showConfirmacionEliminar = false) }
    }

    fun onConfirmarEliminar() {
        _uiState.value.libro?.let {
            viewModelScope.launch {
                libroRepository.eliminarLibro(it)
                // Se actualiza el estado para indicar que el libro fue eliminado, lo que dispara la navegación hacia atrás en la UI.
                _uiState.update { it.copy(libroEliminado = true, showConfirmacionEliminar = false) }
            }
        }
    }
}
