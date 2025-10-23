package com.example.booksly.viewmodel

import androidx.lifecycle.viewmodel.viewModelFactory


import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import com.example.booksly.BookslyApplication



object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Inicializador para LoginViewModel
        initializer {
            LoginViewModel(bookslyApplication().container.usuarioRepository)
        }
        // Inicializador para RegistroViewModel
        initializer {
            RegistroViewModel(bookslyApplication().container.usuarioRepository)
        }
        // Inicializador para InicioViewModel
        initializer {
            InicioViewModel(bookslyApplication().container.libroRepository)
        }
        // Inicializador para AgregarLibroViewModel
        initializer {
            AgregarLibroViewModel(bookslyApplication().container.libroRepository)
        }

        // --- INICIALIZADOR PARA LibroDetalleViewModel (CLAVE) ---
        initializer {
            // Obtiene el SavedStateHandle automáticamente
            val savedStateHandle = createSavedStateHandle()
            LibroDetalleViewModel(
                libroRepository = bookslyApplication().container.libroRepository,
                savedStateHandle = savedStateHandle
            )
        }
        initializer {
            ProgresoViewModel(bookslyApplication().container.libroRepository)
        }
    }
}


fun CreationExtras.bookslyApplication(): BookslyApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BookslyApplication)


