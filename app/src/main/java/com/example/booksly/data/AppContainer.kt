package com.example.booksly.data

import android.content.Context
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.data.repository.NotaRepository
import com.example.booksly.data.repository.PreferenciasRepository
import com.example.booksly.data.repository.UsuarioRepository
import com.example.booksly.model.AppDatabase

interface AppContainer {
    val usuarioRepository: UsuarioRepository
    val libroRepository: LibroRepository
    val notaRepository: NotaRepository // <-- AÑADIDO
    val preferenciasRepository: PreferenciasRepository
}

// Implementación concreta del contenedor de dependencias
class AppDataContainer(private val context: Context) : AppContainer {

    private val appDatabase : AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }
    override val usuarioRepository: UsuarioRepository by lazy {
        UsuarioRepository(appDatabase.usuarioDao())
    }
    override val libroRepository: LibroRepository by lazy {
        LibroRepository(appDatabase.libroDao())
    }
    override val notaRepository: NotaRepository by lazy {
        NotaRepository(appDatabase.notaDao())
    }

    override val preferenciasRepository: PreferenciasRepository by lazy {
        PreferenciasRepository(context)
    }

}
