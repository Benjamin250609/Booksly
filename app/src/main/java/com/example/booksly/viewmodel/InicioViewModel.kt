package com.example.booksly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.model.Libro
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel para la pantalla de Inicio (la estantería de libros).
 * Su principal responsabilidad es proporcionar la lista de libros a la UI.
 */
class InicioViewModel(libroRepository: LibroRepository) : ViewModel() {

    /**
     * Un StateFlow que emite la lista completa de libros del usuario.
     * Se obtiene directamente del repositorio y se convierte en un StateFlow "caliente".
     * Esto significa que la UI puede observar este flujo y se actualizará automáticamente
     * cada vez que la lista de libros cambie en la base de datos.
     */
    val libros: StateFlow<List<Libro>> = libroRepository.obtenerTodosLosLibros()
        .stateIn(
            scope = viewModelScope, // El ciclo de vida del flujo está ligado al del ViewModel.
            started = SharingStarted.WhileSubscribed(5000), // El flujo se mantiene activo 5s después de que la UI deja de observar.
            initialValue = emptyList() // El valor inicial es una lista vacía, que se mostrará mientras se cargan los datos.
        )
}
