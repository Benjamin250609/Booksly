package com.example.booksly.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.data.repository.PreferenciasRepository
import com.example.booksly.data.repository.UsuarioRepository
import com.example.booksly.model.Usuario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


data class PerfilUiState(
    val imagenUri: Uri? = null,
    val nombreUsuario: String = "",
    val emailUsuario: String = "",
    val librosTerminados: Int = 0,
    val paginasLeidas: Int = 0,
)

@OptIn(ExperimentalCoroutinesApi::class)
class PerfilViewModel(
    private val preferenciasRepository: PreferenciasRepository,
    private val usuarioRepository: UsuarioRepository,
    private val libroRepository: LibroRepository
) : ViewModel() {

    private val _logoutState = MutableStateFlow(false)
    val logoutExitoso: StateFlow<Boolean> = _logoutState.asStateFlow()

    private val usuarioFlow: StateFlow<Usuario?> = preferenciasRepository.usuarioEmailFlow.flatMapLatest { email ->
        if (email == null) {
            flowOf(null)
        } else {
            usuarioRepository.getUsuarioPorEmailFlow(email)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val uiState: StateFlow<PerfilUiState> = combine(
        preferenciasRepository.imagenPerfilUriFlow,
        usuarioFlow,
        usuarioFlow.flatMapLatest { usuario ->
            if (usuario == null) flowOf(0) else libroRepository.contarLibrosFinalizados(usuario.id.toLong())
        },
        usuarioFlow.flatMapLatest { usuario ->
            if (usuario == null) flowOf(0) else libroRepository.contarPaginasLeidas(usuario.id.toLong())
        }
    ) { imagenUri, usuario, librosTerminados, paginasLeidas ->
        PerfilUiState(
            imagenUri = imagenUri?.let { Uri.parse(it) },
            nombreUsuario = usuario?.nombre ?: "Usuario",
            emailUsuario = usuario?.email ?: "",
            librosTerminados = librosTerminados,
            paginasLeidas = paginasLeidas
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = PerfilUiState()
    )

    fun onImagenSeleccionada(uri: Uri?) {
        viewModelScope.launch {
            preferenciasRepository.guardarImagenPerfilUri(uri?.toString())
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            preferenciasRepository.guardarUsuarioEmail(null)
            preferenciasRepository.guardarImagenPerfilUri(null)
            _logoutState.value = true
        }
    }
}
