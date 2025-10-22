package com.example.booksly.data.repository


import com.example.booksly.model.Libro
import com.example.booksly.model.LibroDao
import kotlinx.coroutines.flow.Flow

class LibroRepository(private val libroDao: LibroDao) {


    fun obtenerTodosLosLibros(): Flow<List<Libro>> {
        return libroDao.obtenerLibros()
    }


    suspend fun agregarLibro(libro: Libro) {
        libroDao.insertar(libro)
    }


    suspend fun actualizarLibro(libro: Libro) {
        libroDao.actualizar(libro)
    }
}
