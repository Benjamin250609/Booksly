package com.example.booksly.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.booksly.BookslyApplication

object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Inicializador para LoginViewModel
        initializer {
            LoginViewModel(
                usuarioRepository = bookslyApplication().container.usuarioRepository,
                preferenciasRepository = bookslyApplication().container.preferenciasRepository
            )
        }
        // Inicializador para RegistroViewModel
        initializer {
            RegistroViewModel(bookslyApplication().container.usuarioRepository)
        }
        // Inicializador para InicioViewModel
        initializer {
            InicioViewModel(bookslyApplication().container.libroRepository)
        }
        
        // --- INICIALIZADOR PARA AgregarLibroViewModel (ACTUALIZADO) ---
        initializer {
            val savedStateHandle = createSavedStateHandle()
            AgregarLibroViewModel(
                libroRepository = bookslyApplication().container.libroRepository,
                savedStateHandle = savedStateHandle,
                application = bookslyApplication()
            )
        }

        // --- INICIALIZADOR PARA LibroDetalleViewModel (ACTUALIZADO) ---
        initializer {
            val savedStateHandle = createSavedStateHandle()
            LibroDetalleViewModel(
                libroRepository = bookslyApplication().container.libroRepository,
                notaRepository = bookslyApplication().container.notaRepository,
                savedStateHandle = savedStateHandle
            )
        }
        initializer {
            BuscarViewModel(bookslyApplication().container.libroRepository)
        }

        // --- INICIALIZADOR PARA PerfilViewModel (ACTUALIZADO) ---
        initializer {
            PerfilViewModel(
                preferenciasRepository = bookslyApplication().container.preferenciasRepository,
                usuarioRepository = bookslyApplication().container.usuarioRepository,
                libroRepository = bookslyApplication().container.libroRepository
            )
        }

        initializer {
            ProgresoViewModel(bookslyApplication().container.libroRepository)
        }
    }
}

fun CreationExtras.bookslyApplication(): BookslyApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BookslyApplication)
