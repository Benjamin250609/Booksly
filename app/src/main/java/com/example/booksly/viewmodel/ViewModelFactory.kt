package com.example.booksly.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.booksly.BookslyApplication

object AppViewModelProvider {

    val Factory = viewModelFactory {

        initializer {
            LoginViewModel(
                authRepository = bookslyApplication().container.authRepository,
                usuarioRepository = bookslyApplication().container.usuarioRepository,
                preferenciasRepository = bookslyApplication().container.preferenciasRepository
            )
        }

        initializer {
            RegistroViewModel(
                authRepository = bookslyApplication().container.authRepository,
                usuarioRepository = bookslyApplication().container.usuarioRepository
            )
        }

        initializer {
            InicioViewModel(
                libroRepository = bookslyApplication().container.libroRepository,
                usuarioRepository = bookslyApplication().container.usuarioRepository,
                preferenciasRepository = bookslyApplication().container.preferenciasRepository
            )
        }
        
        initializer {
            val savedStateHandle = createSavedStateHandle()
            AgregarLibroViewModel(
                libroRepository = bookslyApplication().container.libroRepository,
                usuarioRepository = bookslyApplication().container.usuarioRepository,
                preferenciasRepository = bookslyApplication().container.preferenciasRepository,
                booksApiService = bookslyApplication().container.booksApiService,
                savedStateHandle = savedStateHandle,
                application = bookslyApplication()
            )
        }

        initializer {
            val savedStateHandle = createSavedStateHandle()
            LibroDetalleViewModel(
                libroRepository = bookslyApplication().container.libroRepository,
                notaRepository = bookslyApplication().container.notaRepository,
                savedStateHandle = savedStateHandle
            )
        }

        initializer {
            BuscarViewModel(
                libroRepository = bookslyApplication().container.libroRepository,
                usuarioRepository = bookslyApplication().container.usuarioRepository,
                preferenciasRepository = bookslyApplication().container.preferenciasRepository
            )
        }

        initializer {
            PerfilViewModel(
                preferenciasRepository = bookslyApplication().container.preferenciasRepository,
                usuarioRepository = bookslyApplication().container.usuarioRepository,
                libroRepository = bookslyApplication().container.libroRepository
            )
        }

        initializer {
            ProgresoViewModel(
                libroRepository = bookslyApplication().container.libroRepository,
                usuarioRepository = bookslyApplication().container.usuarioRepository,
                preferenciasRepository = bookslyApplication().container.preferenciasRepository
            )
        }
    }
}

fun CreationExtras.bookslyApplication(): BookslyApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BookslyApplication)
