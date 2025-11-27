package com.example.booksly.data.repository

import com.example.booksly.model.Libro
import com.example.booksly.model.LibroDao
import kotlinx.coroutines.flow.Flow

class LibroRepository(private val libroDao: LibroDao) {

    fun obtenerTodosLosLibros(userId: Long): Flow<List<Libro>> {
        return libroDao.obtenerLibros(userId)
    }

    fun obtenerLibrosPorEstado(userId: Long, estado: String): Flow<List<Libro>> {
        return libroDao.obtenerLibrosPorEstado(userId, estado)
    }

    suspend fun agregarLibro(libro: Libro) {
        libroDao.insertar(libro)
    }

    suspend fun actualizarLibro(libro: Libro) {
        libroDao.actualizar(libro)
    }

    suspend fun eliminarLibro(libro: Libro) {
        libroDao.eliminar(libro)
    }

    fun obtenerLibroPorId(libroId: Int): Flow<Libro?> {
        return libroDao.obtenerLibroPorId(libroId)
    }

    fun contarLibrosFinalizados(userId: Long): Flow<Int> {
        return libroDao.contarLibrosFinalizados(userId)
    }

    fun contarPaginasLeidas(userId: Long): Flow<Int> {
        return libroDao.contarPaginasLeidas(userId)
    }

    fun buscarLibrosPorTermino(userId: Long, termino: String): Flow<List<Libro>> {
        return libroDao.buscarLibrosPorTermino(userId, termino)
    }
}
