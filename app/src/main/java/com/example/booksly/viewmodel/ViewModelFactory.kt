package com.example.booksly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.data.repository.UsuarioRepository
import kotlin.jvm.java

class ViewModelFactory(
    private val usuarioRepository: UsuarioRepository,
    private val libroRepository: LibroRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(usuarioRepository) as T
        }
        // Comprueba si se pide RegistroViewModel
        if (modelClass.isAssignableFrom(RegistroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegistroViewModel(usuarioRepository) as T
        }
        // Comprueba si se pide InicioViewModel
        if (modelClass.isAssignableFrom(InicioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InicioViewModel(libroRepository) as T
        }
        if (modelClass.isAssignableFrom(AgregarLibroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AgregarLibroViewModel(libroRepository) as T
        }
        // Si no es ninguno de los conocidos, lanza un error
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
