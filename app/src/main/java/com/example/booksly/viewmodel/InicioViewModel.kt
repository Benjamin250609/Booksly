package com.example.booksly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.model.Libro
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn


class InicioViewModel(libroRepository: LibroRepository) : ViewModel() {


    val libros: StateFlow<List<Libro>> = libroRepository.obtenerTodosLosLibros()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
