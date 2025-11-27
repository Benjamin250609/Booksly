package com.example.booksly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.data.repository.PreferenciasRepository
import com.example.booksly.data.repository.UsuarioRepository
import com.example.booksly.model.Libro
import com.example.booksly.model.Usuario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

data class BuscarUiState(
    val terminoBusqueda: String = "",
    val resultados: List<Libro> = emptyList(),
    val isLoading: Boolean = false,
    val sinResultados: Boolean = false
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class BuscarViewModel(
    private val libroRepository: LibroRepository,
    private val usuarioRepository: UsuarioRepository,
    private val preferenciasRepository: PreferenciasRepository
) : ViewModel() {

    private val _terminoBusqueda = MutableStateFlow("")

    private val usuarioFlow: StateFlow<Usuario?> = preferenciasRepository.usuarioEmailFlow.flatMapLatest { email ->
        if (email == null) flowOf(null) else usuarioRepository.getUsuarioPorEmailFlow(email)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _resultadosBusqueda: StateFlow<List<Libro>> = combine(
        _terminoBusqueda.debounce(300L).distinctUntilChanged(),
        usuarioFlow
    ) { termino, usuario ->
        if (termino.trim().isNotEmpty() && usuario != null) {
            libroRepository.buscarLibrosPorTermino(usuario.id.toLong(), termino.trim())
        } else {
            flowOf(emptyList())
        }
    }.flatMapLatest { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val uiState: StateFlow<BuscarUiState> = combine(
        _terminoBusqueda,
        _resultadosBusqueda
    ) { termino, resultados ->
        BuscarUiState(
            terminoBusqueda = termino,
            resultados = resultados,
            sinResultados = termino.trim().isNotEmpty() && resultados.isEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = BuscarUiState()
    )

    fun onTerminoBusquedaChange(nuevoTermino: String) {
        _terminoBusqueda.value = nuevoTermino
    }
}
