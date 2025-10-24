package com.example.booksly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.model.Libro
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

// Estado de la UI para la pantalla de Búsqueda
data class BuscarUiState(
    val terminoBusqueda: String = "",
    val resultados: List<Libro> = emptyList(),
    val isLoading: Boolean = false,
    val sinResultados: Boolean = false
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class BuscarViewModel(private val libroRepository: LibroRepository) : ViewModel() {

    // Flow privado para el término de búsqueda actual
    private val _terminoBusqueda = MutableStateFlow("")
    val terminoBusqueda: StateFlow<String> = _terminoBusqueda.asStateFlow()

    // Flow que reacciona a los cambios en _terminoBusqueda, espera un poco,
    // y luego lanza la búsqueda en la base de datos.
    private val _resultadosBusqueda: StateFlow<List<Libro>> = _terminoBusqueda
        // No buscar si está vacío después de quitar espacios
        .filter { it.trim().isNotEmpty() }
        // Espera 300ms después de que el usuario deja de escribir antes de buscar
        .debounce(300L)
        // Evita búsquedas duplicadas si el término no cambió realmente
        .distinctUntilChanged()
        // Ejecuta la búsqueda en la base de datos (flatMapLatest cancela búsquedas anteriores si llega un nuevo término)
        .flatMapLatest { termino ->
            libroRepository.buscarLibrosPorTermino(termino.trim())
            // Puedes añadir .catch { } aquí para manejar errores de BD si fuera necesario
        }
        // Convierte el Flow resultante en un StateFlow para la UI
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // Combina el término de búsqueda y los resultados en el estado final de la UI
    val uiState: StateFlow<BuscarUiState> = combine(
        _terminoBusqueda,
        _resultadosBusqueda
    ) { termino, resultados ->
        BuscarUiState(
            terminoBusqueda = termino,
            resultados = resultados,
            // Considera "sin resultados" solo si se ha buscado algo y la lista está vacía
            sinResultados = termino.trim().isNotEmpty() && resultados.isEmpty()
            // isLoading podría manejarse de forma más compleja si la búsqueda fuera en red
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = BuscarUiState()
    )


    // Función llamada por la UI cuando el texto de búsqueda cambia
    fun onTerminoBusquedaChange(nuevoTermino: String) {
        _terminoBusqueda.value = nuevoTermino
    }
}
