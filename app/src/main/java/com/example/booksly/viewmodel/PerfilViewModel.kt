package com.example.booksly.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.data.repository.PreferenciasRepository
import com.example.booksly.data.repository.UsuarioRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la UI para la pantalla de Perfil (más completo)
data class PerfilUiState(
    val imagenUri: Uri? = null,
    val nombreUsuario: String = "",
    val emailUsuario: String = "",
    val librosTerminados: Int = 0,
    val paginasLeidas: Int = 0,
    val logoutExitoso: Boolean = false // Para notificar a la UI que navegue
)

@OptIn(ExperimentalCoroutinesApi::class)
class PerfilViewModel(
    private val preferenciasRepository: PreferenciasRepository,
    private val usuarioRepository: UsuarioRepository,
    private val libroRepository: LibroRepository
) : ViewModel() {

    // Combina todos los flujos de datos en un único estado de la UI
    val uiState: StateFlow<PerfilUiState> = combine(
        preferenciasRepository.imagenPerfilUriFlow,
        preferenciasRepository.usuarioEmailFlow.flatMapLatest { email ->
            usuarioRepository.buscarUsuarioPorEmailFlow(email ?: "")
        },
        libroRepository.contarLibrosFinalizados(),
        libroRepository.contarPaginasLeidas()
    ) { imagenUri, usuario, librosTerminados, paginasLeidas ->
        PerfilUiState(
            imagenUri = imagenUri?.let { Uri.parse(it) },
            nombreUsuario = usuario?.nombre ?: "Usuario",
            emailUsuario = usuario?.correo ?: "",
            librosTerminados = librosTerminados,
            paginasLeidas = paginasLeidas ?: 0
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = PerfilUiState() // Estado inicial por defecto
    )

    private val _logoutState = MutableStateFlow(false)
    val logoutExitoso: StateFlow<Boolean> = _logoutState.asStateFlow()

    // Guarda la nueva imagen de perfil seleccionada
    fun onImagenSeleccionada(uri: Uri?) {
        viewModelScope.launch {
            preferenciasRepository.guardarImagenPerfilUri(uri?.toString())
        }
    }

    // Cierra la sesión del usuario
    fun cerrarSesion() {
        viewModelScope.launch {
            // Limpia los datos guardados en las preferencias
            preferenciasRepository.guardarUsuarioEmail(null)
            preferenciasRepository.guardarImagenPerfilUri(null)
            _logoutState.value = true // Notifica a la UI que el logout fue exitoso
        }
    }
}
