package com.example.booksly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.model.Libro
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine // Para combinar múltiples flows
import kotlinx.coroutines.flow.stateIn

// Data class para contener todas las estadísticas juntas
data class EstadisticasUiState(
    val librosFinalizadosCount: Int = 0,
    val totalPaginasLeidas: Int = 0,
    val librosEnCurso: List<Libro> = emptyList(),
    val librosFinalizadosList: List<Libro> = emptyList()
)

class ProgresoViewModel(private val libroRepository: LibroRepository) : ViewModel() {


    val estadisticasUiState: StateFlow<EstadisticasUiState> = combine(
        libroRepository.contarLibrosFinalizados(),
        libroRepository.contarPaginasLeidas(),
        libroRepository.obtenerTodosLosLibros()
    ) { finalizadosCount, paginasLeidas, todosLosLibros ->


        val enCurso = todosLosLibros.filter { it.estado == "leyendo" }
        val finalizados = todosLosLibros.filter { it.estado == "finalizado" }


        EstadisticasUiState(
            librosFinalizadosCount = finalizadosCount,
            totalPaginasLeidas = paginasLeidas ?: 0,
            librosEnCurso = enCurso,
            librosFinalizadosList = finalizados
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = EstadisticasUiState() // Estado inicial vacío
    )
}
