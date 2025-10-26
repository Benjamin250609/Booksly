package com.example.booksly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.model.Libro
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine // Para combinar múltiples flows
import kotlinx.coroutines.flow.stateIn

/**
 * Data class que representa el estado de la UI para la pantalla de Progreso.
 * Contiene todas las estadísticas que se mostrarán al usuario.
 */
data class EstadisticasUiState(
    val librosFinalizadosCount: Int = 0,
    val totalPaginasLeidas: Int = 0,
    val librosEnCurso: List<Libro> = emptyList(),
    val librosFinalizadosList: List<Libro> = emptyList()
)

/**
 * ViewModel para la pantalla de Progreso.
 * Recopila y calcula estadísticas de lectura del usuario.
 */
class ProgresoViewModel(private val libroRepository: LibroRepository) : ViewModel() {

    /**
     * Un StateFlow que emite el estado completo de las estadísticas de la UI.
     * Se crea combinando varios flujos del repositorio, lo que lo hace muy reactivo y eficiente.
     */
    val estadisticasUiState: StateFlow<EstadisticasUiState> = combine(
        // Flujos de origen que se van a combinar:
        libroRepository.contarLibrosFinalizados(),
        libroRepository.contarPaginasLeidas(),
        libroRepository.obtenerTodosLosLibros()
    ) { finalizadosCount, paginasLeidas, todosLosLibros ->
        // Este bloque se ejecuta cada vez que cualquiera de los flujos de origen emite un nuevo valor.

        // Filtramos la lista de todos los libros para obtener los que están en curso y los finalizados.
        val enCurso = todosLosLibros.filter { it.estado == "leyendo" }
        val finalizados = todosLosLibros.filter { it.estado == "finalizado" }

        // Creamos el objeto de estado con los datos combinados y calculados.
        EstadisticasUiState(
            librosFinalizadosCount = finalizadosCount,
            totalPaginasLeidas = paginasLeidas, // El DAO ya devuelve 0 si es nulo gracias a COALESCE.
            librosEnCurso = enCurso,
            librosFinalizadosList = finalizados
        )
    }.stateIn(
        // Convertimos el flujo combinado en un StateFlow.
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = EstadisticasUiState() // Estado inicial mientras se cargan los datos.
    )
}
