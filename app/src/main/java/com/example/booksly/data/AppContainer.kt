package com.example.booksly.data

import android.content.Context
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.data.repository.NotaRepository
import com.example.booksly.data.repository.PreferenciasRepository
import com.example.booksly.data.repository.UsuarioRepository
import com.example.booksly.model.AppDatabase

/**
 * Interfaz para el contenedor de dependencias de la aplicación.
 * Define los repositorios que estarán disponibles para ser inyectados en los ViewModels.
 */
interface AppContainer {
    val usuarioRepository: UsuarioRepository
    val libroRepository: LibroRepository
    val notaRepository: NotaRepository
    val preferenciasRepository: PreferenciasRepository
}

/**
 * Implementación concreta del contenedor de dependencias.
 * Se encarga de crear y proveer las instancias de los repositorios, asegurando que
 * solo se cree una instancia de cada uno gracias al uso de `lazy`.
 */
class AppDataContainer(private val context: Context) : AppContainer {

    // Instancia de la base de datos de Room, creada de forma perezosa la primera vez que se necesita.
    private val appDatabase : AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    // Repositorio para los usuarios, inyectando el DAO correspondiente desde la base de datos.
    override val usuarioRepository: UsuarioRepository by lazy {
        UsuarioRepository(appDatabase.usuarioDao())
    }

    // Repositorio para los libros.
    override val libroRepository: LibroRepository by lazy {
        LibroRepository(appDatabase.libroDao())
    }

    // Repositorio para las notas.
    override val notaRepository: NotaRepository by lazy {
        NotaRepository(appDatabase.notaDao())
    }

    // Repositorio para gestionar las preferencias del usuario (DataStore).
    override val preferenciasRepository: PreferenciasRepository by lazy {
        PreferenciasRepository(context)
    }

}
