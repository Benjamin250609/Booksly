package com.example.booksly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.data.repository.PreferenciasRepository
import com.example.booksly.data.repository.UsuarioRepository
import com.example.booksly.model.Libro
import com.example.booksly.model.Usuario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class EstadisticasUiState(
    val librosFinalizadosCount: Int = 0,
    val totalPaginasLeidas: Int = 0,
    val librosEnCurso: List<Libro> = emptyList(),
    val librosFinalizadosList: List<Libro> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProgresoViewModel(
    private val libroRepository: LibroRepository,
    private val usuarioRepository: UsuarioRepository,
    private val preferenciasRepository: PreferenciasRepository
) : ViewModel() {

    private val usuarioFlow: StateFlow<Usuario?> = preferenciasRepository.usuarioEmailFlow.flatMapLatest { email ->
        if (email == null) flowOf(null) else usuarioRepository.getUsuarioPorEmailFlow(email)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val estadisticasUiState: StateFlow<EstadisticasUiState> = usuarioFlow.flatMapLatest { usuario ->
        if (usuario == null) {
            flowOf(EstadisticasUiState())
        } else {
            combine(
                libroRepository.contarLibrosFinalizados(usuario.id.toLong()),
                libroRepository.contarPaginasLeidas(usuario.id.toLong()),
                libroRepository.obtenerLibrosPorEstado(usuario.id.toLong(), "leyendo"),
                libroRepository.obtenerLibrosPorEstado(usuario.id.toLong(), "finalizado")
            ) { finalizadosCount, paginasLeidas, enCurso, finalizados ->
                EstadisticasUiState(
                    librosFinalizadosCount = finalizadosCount,
                    totalPaginasLeidas = paginasLeidas,
                    librosEnCurso = enCurso,
                    librosFinalizadosList = finalizados
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = EstadisticasUiState()
    )
}
