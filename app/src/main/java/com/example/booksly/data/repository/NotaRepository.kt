package com.example.booksly.data.repository

import com.example.booksly.model.Nota
import com.example.booksly.model.NotaDao
import kotlinx.coroutines.flow.Flow

class NotaRepository(private val notaDao: NotaDao) {

    fun obtenerNotasPorLibro(libroId: Int): Flow<List<Nota>> {
        return notaDao.obtenerNotasPorLibro(libroId)
    }

    suspend fun insertarNota(nota: Nota) {
        notaDao.insertar(nota)
    }

    suspend fun eliminarNota(nota: Nota) {
        notaDao.eliminar(nota)
    }
}
