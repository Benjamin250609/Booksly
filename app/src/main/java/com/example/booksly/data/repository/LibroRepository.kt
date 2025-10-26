package com.example.booksly.data.repository


import com.example.booksly.model.Libro
import com.example.booksly.model.LibroDao
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio que gestiona las operaciones de datos para [Libro].
 * Abstrae el acceso a la fuente de datos (Room), permitiendo
 * que los ViewModels interactúen con los datos de los libros.
 */
class LibroRepository(private val libroDao: LibroDao) {

    /**
     * Obtiene todos los libros de la base de datos como un [Flow].
     * La UI puede observar este flujo para reaccionar a los cambios en la lista de libros.
     */
    fun obtenerTodosLosLibros(): Flow<List<Libro>> {
        return libroDao.obtenerLibros()
    }

    /**
     * Inserta un nuevo libro en la base de datos.
     */
    suspend fun agregarLibro(libro: Libro) {
        libroDao.insertar(libro)
    }

    /**
     * Actualiza un libro existente en la base de datos.
     */
    suspend fun actualizarLibro(libro: Libro) {
        libroDao.actualizar(libro)
    }

    /**
     * Elimina un libro de la base de datos.
     */
    suspend fun eliminarLibro(libro: Libro) {
        libroDao.eliminar(libro)
    }

    /**
     * Obtiene un libro específico por su ID como un [Flow].
     */
    fun obtenerLibroPorId(libroId: Int): Flow<Libro?> {
        return libroDao.obtenerLibroPorId(libroId)
    }

    /**
     * Cuenta el número total de libros marcados como "leído".
     * Devuelve el resultado como un [Flow] para observación reactiva.
     */
    fun contarLibrosFinalizados(): Flow<Int> {
        return libroDao.contarLibrosFinalizados()
    }

    /**
     * Suma el total de páginas leídas de todos los libros.
     * Devuelve el resultado como un [Flow].
     */
    fun contarPaginasLeidas(): Flow<Int> {
        return libroDao.contarPaginasLeidas()
    }

    /**
     * Busca libros cuyo título o autor coincida con un término de búsqueda.
     * Devuelve una lista de libros como un [Flow].
     */
    fun buscarLibrosPorTermino(termino: String): Flow<List<Libro>> = libroDao.buscarLibrosPorTermino(termino)


}
