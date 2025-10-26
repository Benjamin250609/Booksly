package com.example.booksly.data.repository

import com.example.booksly.model.Nota
import com.example.booksly.model.NotaDao
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para gestionar las operaciones de datos de [Nota].
 * Abstrae el acceso a la fuente de datos (Room)
 * que los ViewModels interactúen con los datos de las notas.
 */
class NotaRepository(private val notaDao: NotaDao) {

    /**
     * Obtiene todas las notas asociadas a un libro específico.
     * Devuelve el resultado como un [Flow]
     *
     * @param libroId El ID del libro para el que se quieren obtener las notas.
     */
    fun obtenerNotasPorLibro(libroId: Int): Flow<List<Nota>> {
        return notaDao.obtenerNotasPorLibro(libroId)
    }

    /**
     * Inserta una nueva nota en la base de datos.
     * Es una función de suspensión, por lo que debe llamarse desde una corrutina.
     *
     * @param nota La nota que se va to insertar.
     */
    suspend fun insertarNota(nota: Nota) {
        notaDao.insertar(nota)
    }

    /**
     * Elimina una nota de la base de datos.
     * Es una función de suspensión.
     *
     * @param nota La nota que se va to eliminar.
     */
    suspend fun eliminarNota(nota: Nota) {
        notaDao.eliminar(nota)
    }
}
