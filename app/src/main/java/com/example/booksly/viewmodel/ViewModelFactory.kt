package com.example.booksly.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.booksly.BookslyApplication

/**
 * Objeto singleton que proporciona una fábrica (`Factory`) para crear todas las instancias de ViewModel de la aplicación.
 * Este es el punto central para la inyección de dependencias en los ViewModels.
 */
object AppViewModelProvider {

    // La fábrica que se usará en la UI para obtener las instancias de los ViewModels.
    val Factory = viewModelFactory {

        // Inicializador para LoginViewModel.
        // Obtiene las dependencias necesarias del contenedor de la aplicación.
        initializer {
            LoginViewModel(
                usuarioRepository = bookslyApplication().container.usuarioRepository,
                preferenciasRepository = bookslyApplication().container.preferenciasRepository
            )
        }

        // Inicializador para RegistroViewModel.
        initializer {
            RegistroViewModel(bookslyApplication().container.usuarioRepository)
        }

        // Inicializador para InicioViewModel.
        initializer {
            InicioViewModel(bookslyApplication().container.libroRepository)
        }
        
        // Inicializador para AgregarLibroViewModel.
        // Utiliza createSavedStateHandle() para poder acceder a los argumentos de navegación (p. ej., el ID del libro a editar).
        initializer {
            val savedStateHandle = createSavedStateHandle()
            AgregarLibroViewModel(
                libroRepository = bookslyApplication().container.libroRepository,
                savedStateHandle = savedStateHandle,
                application = bookslyApplication()
            )
        }

        // Inicializador para LibroDetalleViewModel.
        initializer {
            val savedStateHandle = createSavedStateHandle()
            LibroDetalleViewModel(
                libroRepository = bookslyApplication().container.libroRepository,
                notaRepository = bookslyApplication().container.notaRepository,
                savedStateHandle = savedStateHandle
            )
        }

        // Inicializador para BuscarViewModel.
        initializer {
            BuscarViewModel(bookslyApplication().container.libroRepository)
        }

        // Inicializador para PerfilViewModel.
        initializer {
            PerfilViewModel(
                preferenciasRepository = bookslyApplication().container.preferenciasRepository,
                usuarioRepository = bookslyApplication().container.usuarioRepository,
                libroRepository = bookslyApplication().container.libroRepository
            )
        }

        // Inicializador para ProgresoViewModel.
        initializer {
            ProgresoViewModel(bookslyApplication().container.libroRepository)
        }
    }
}

/**
 * Función de extensión para [CreationExtras] que permite obtener de forma segura la instancia de [BookslyApplication].
 * Esto es necesario para acceder al contenedor de dependencias (`container`) desde los inicializadores.
 */
fun CreationExtras.bookslyApplication(): BookslyApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BookslyApplication)
