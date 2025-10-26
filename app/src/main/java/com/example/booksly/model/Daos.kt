package com.example.booksly.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


/**
 * DAO (Data Access Object) para la entidad [Usuario].
 * Define los métodos para interactuar con la tabla "usuarios" en la base de datos.
 */
@Dao
interface UsuarioDao {

    /**
     * Inserta un usuario. Si el usuario ya existe, lo reemplaza.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(usuario: Usuario)

    /**
     * Busca un usuario por su email. Devuelve un objeto [Usuario] o null si no lo encuentra.
     * Es una función de suspensión para ser llamada desde una corrutina.
     */
    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun buscarPorCorreo(email: String): Usuario?

    /**
     * Busca un usuario por su email y devuelve el resultado como un [Flow].
     * Permite observar cambios en el usuario de forma reactiva.
     */
    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    fun buscarPorCorreoFlow(email: String): Flow<Usuario?>
}


/**
 * DAO para la entidad [Libro].
 * Define los métodos para interactuar con la tabla "libros".
 */
@Dao
interface LibroDao {
    /**
     * Inserta un libro. Si ya existe, lo reemplaza.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(libro: Libro)

    /**
     * Actualiza un libro existente.
     */
    @Update
    suspend fun actualizar(libro: Libro)

    /**
     * Elimina un libro.
     */
    @Delete
    suspend fun eliminar(libro: Libro)

    /**
     * Obtiene todos los libros ordenados por ID de forma descendente, como un [Flow].
     */
    @Query("SELECT * FROM libros ORDER BY id DESC")
    fun obtenerLibros(): Flow<List<Libro>>

    /**
     * Obtiene un libro por su ID como un [Flow].
     */
    @Query("SELECT * FROM libros WHERE id = :libroId LIMIT 1")
    fun obtenerLibroPorId(libroId: Int): Flow<Libro?>

    /**
     * Cuenta cuántos libros tienen el estado "finalizado" y devuelve el total como un [Flow].
     */
    @Query("SELECT COUNT(*) FROM libros WHERE estado = 'leído'")
    fun contarLibrosFinalizados(): Flow<Int>

    /**
     * Suma las páginas totales de los libros con estado "finalizado".
     * COALESCE se usa para devolver 0 si no hay libros finalizados.
     */
    @Query("SELECT COALESCE(SUM(totalPaginas), 0) FROM libros WHERE estado = 'leído'")
    fun contarPaginasLeidas(): Flow<Int>

    /**
     * Busca libros por título o autor, ignorando mayúsculas y minúsculas.
     * Devuelve los resultados como un [Flow].
     */
    @Query("SELECT * FROM libros WHERE titulo LIKE '%' || :termino || '%' OR autor LIKE '%' || :termino || '%' ORDER BY titulo ASC")
    fun buscarLibrosPorTermino(termino: String): Flow<List<Libro>>
}

/**
 * DAO para la entidad [Nota].
 * Define los métodos para interactuar con la tabla "notas".
 */
@Dao
interface NotaDao {
    /**
     * Inserta una nota. Si ya existe, la reemplaza.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(nota: Nota)

    /**
     * Elimina una nota.
     */
    @Delete
    suspend fun eliminar(nota: Nota)

    /**
     * Obtiene todas las notas de un libro específico, ordenadas por fecha descendente.
     * Devuelve el resultado como un [Flow].
     */
    @Query("SELECT * FROM notas WHERE libroId = :libroId ORDER BY id DESC")
    fun obtenerNotasPorLibro(libroId: Int): Flow<List<Nota>>
}
