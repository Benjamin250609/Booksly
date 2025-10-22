package com.example.booksly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.booksly.data.repository.UsuarioRepository

class ViewModelFactory(private val usuarioRepository: UsuarioRepository) : ViewModelProvider.Factory {


    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(RegistroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegistroViewModel(usuarioRepository) as T
        }
        // Comprueba si la clase solicitada es LoginViewModel
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(usuarioRepository) as T
        }
        // Si no es ninguna de las anteriores, lanza una excepción.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}