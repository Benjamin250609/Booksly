package com.example.booksly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.model.Libro
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

/**
 * Data class que representa el estado de la UI para la pantalla de Búsqueda.
 */
data class BuscarUiState(
    val terminoBusqueda: String = "",
    val resultados: List<Libro> = emptyList(),
    val isLoading: Boolean = false, // Útil si la búsqueda fuera en red; aquí es casi instantánea.
    val sinResultados: Boolean = false // True si se ha buscado algo pero no se encontraron libros.
)

/**
 * ViewModel para la pantalla de búsqueda.
 * Utiliza flujos de corrutinas para realizar búsquedas de libros de forma reactiva y eficiente.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class BuscarViewModel(private val libroRepository: LibroRepository) : ViewModel() {

    // Flujo mutable y privado que contiene el término de búsqueda actual introducido por el usuario.
    private val _terminoBusqueda = MutableStateFlow("")

    /**
     * Transforma el flujo del término de búsqueda en un flujo de resultados.
     * Esta es la parte central de la lógica reactiva.
     */
    private val _resultadosBusqueda: StateFlow<List<Libro>> = _terminoBusqueda
        // No se inicia la búsqueda si el término está en blanco.
        .filter { it.trim().isNotEmpty() }
        // Espera 300ms después de que el usuario deja de escribir para no sobrecargar la base de datos con búsquedas.
        .debounce(300L)
        // Evita realizar la misma búsqueda dos veces seguidas.
        .distinctUntilChanged()
        // Realiza la búsqueda. flatMapLatest cancela la búsqueda anterior si el usuario escribe un nuevo término.
        .flatMapLatest { termino ->
            libroRepository.buscarLibrosPorTermino(termino.trim())
        }
        // Convierte el flujo "frío" en un flujo "caliente" (StateFlow) que mantiene el último valor
        // y lo comparte entre todos los observadores (la UI).
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L), // El flujo se mantiene activo 5s después de que la UI deja de observar.
            initialValue = emptyList()
        )

    /**
     * Combina el término de búsqueda actual y los resultados de la búsqueda en un único objeto de estado
     */
    val uiState: StateFlow<BuscarUiState> = combine(
        _terminoBusqueda,
        _resultadosBusqueda
    ) { termino, resultados ->
        BuscarUiState(
            terminoBusqueda = termino,
            resultados = resultados,
            // Se considera "sin resultados" solo si se ha buscado activamente (término no vacío) y la lista de resultados está vacía.
            sinResultados = termino.trim().isNotEmpty() && resultados.isEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = BuscarUiState()
    )


    /**
     * Función llamada por la UI para actualizar el término de búsqueda.
     */
    fun onTerminoBusquedaChange(nuevoTermino: String) {
        _terminoBusqueda.value = nuevoTermino
    }
}
