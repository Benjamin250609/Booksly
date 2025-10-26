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
import kotlinx.coroutines.launch

/**
 * Data class que representa el estado de la UI para la pantalla de Perfil.
 */
data class PerfilUiState(
    val imagenUri: Uri? = null,
    val nombreUsuario: String = "",
    val emailUsuario: String = "",
    val librosTerminados: Int = 0,
    val paginasLeidas: Int = 0,
)

/**
 * ViewModel para la pantalla de Perfil.
 * Combina datos de múltiples repositorios para mostrar un resumen del perfil del usuario,
 * sus estadísticas y gestionar acciones como cambiar la foto o cerrar sesión.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PerfilViewModel(
    private val preferenciasRepository: PreferenciasRepository,
    private val usuarioRepository: UsuarioRepository,
    private val libroRepository: LibroRepository
) : ViewModel() {

    // Flujo para comunicar a la UI que se ha cerrado sesión y debe navegar.
    private val _logoutState = MutableStateFlow(false)
    val logoutExitoso: StateFlow<Boolean> = _logoutState.asStateFlow()

    /**
     * Un StateFlow que emite el estado completo de la UI del perfil.
     * Es un ejemplo avanzado de combine que usa flatMapLatest para crear una cadena reactiva.
     */
    val uiState: StateFlow<PerfilUiState> = combine(
        // 1. Flujo para la URI de la imagen de perfil.
        preferenciasRepository.imagenPerfilUriFlow,
        // 2. Flujo complejo para los datos del usuario.
        preferenciasRepository.usuarioEmailFlow.flatMapLatest { email ->
            // flatMapLatest escucha el email del usuario logueado. Si cambia,
            // cancela la suscripción anterior y crea una nueva al flujo de datos del nuevo usuario.
            usuarioRepository.buscarUsuarioPorEmailFlow(email ?: "")
        },
        // 3. Flujo para el contador de libros terminados.
        libroRepository.contarLibrosFinalizados(),
        // 4. Flujo para el contador de páginas leídas.
        libroRepository.contarPaginasLeidas()
    ) { imagenUri, usuario, librosTerminados, paginasLeidas ->
        // Este bloque se ejecuta cuando cualquiera de los 4 flujos de origen emite un nuevo valor.
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
        initialValue = PerfilUiState() // Estado inicial por defecto.
    )

    /**
     * Se llama cuando el usuario selecciona una nueva imagen de perfil.
     * Guarda la URI de la imagen en las preferencias.
     */
    fun onImagenSeleccionada(uri: Uri?) {
        viewModelScope.launch {
            preferenciasRepository.guardarImagenPerfilUri(uri?.toString())
        }
    }

    /**
     * Cierra la sesión del usuario.
     * Limpia todos los datos guardados en las preferencias y notifica a la UI que el logout fue exitoso.
     */
    fun cerrarSesion() {
        viewModelScope.launch {
            preferenciasRepository.guardarUsuarioEmail(null)
            preferenciasRepository.guardarImagenPerfilUri(null)
            _logoutState.value = true // La UI observará este cambio y navegará al login.
        }
    }
}
