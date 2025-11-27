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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class InicioViewModel(
    private val libroRepository: LibroRepository,
    private val usuarioRepository: UsuarioRepository,
    private val preferenciasRepository: PreferenciasRepository
) : ViewModel() {

    private val usuarioFlow: StateFlow<Usuario?> = preferenciasRepository.usuarioEmailFlow
        .flatMapLatest { email ->
            if (email == null) {
                flowOf(null)
            } else {
                usuarioRepository.getUsuarioPorEmailFlow(email)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val libros: StateFlow<List<Libro>> = usuarioFlow
        .flatMapLatest { usuario ->
            if (usuario == null) {
                flowOf(emptyList())
            } else {
                libroRepository.obtenerTodosLosLibros(usuario.id.toLong())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
