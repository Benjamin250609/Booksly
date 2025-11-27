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
 */
@Dao
interface UsuarioDao {

    /**
     * Inserta un usuario. Si el usuario ya existe, lo reemplaza.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(usuario: Usuario)

    /**
     * Busca un usuario por su email. Devuelve un objeto [Usuario] o null si no lo encuentra.
     */
    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): Usuario?

    /**
     * Busca un usuario por su email y devuelve el resultado como un [Flow].
     */
    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    fun buscarPorEmailFlow(email: String): Flow<Usuario?>
}

/**
 * DAO para la entidad [Libro].
 */
@Dao
interface LibroDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(libro: Libro)

    @Update
    suspend fun actualizar(libro: Libro)

    @Delete
    suspend fun eliminar(libro: Libro)

    @Query("SELECT * FROM libros WHERE userId = :userId ORDER BY id DESC")
    fun obtenerLibros(userId: Long): Flow<List<Libro>>

    @Query("SELECT * FROM libros WHERE userId = :userId AND estado = :estado ORDER BY id DESC")
    fun obtenerLibrosPorEstado(userId: Long, estado: String): Flow<List<Libro>>

    @Query("SELECT * FROM libros WHERE id = :libroId LIMIT 1")
    fun obtenerLibroPorId(libroId: Int): Flow<Libro?>

    @Query("SELECT COUNT(*) FROM libros WHERE userId = :userId AND estado = 'finalizado'")
    fun contarLibrosFinalizados(userId: Long): Flow<Int>

    @Query("SELECT COALESCE((SELECT SUM(totalPaginas) FROM libros WHERE userId = :userId AND estado = 'finalizado'), 0) + COALESCE((SELECT SUM(paginaActual) FROM libros WHERE userId = :userId AND estado = 'leyendo'), 0)")
    fun contarPaginasLeidas(userId: Long): Flow<Int>

    @Query("SELECT * FROM libros WHERE userId = :userId AND (titulo LIKE '%' || :termino || '%' OR autor LIKE '%' || :termino || '%') ORDER BY titulo ASC")
    fun buscarLibrosPorTermino(userId: Long, termino: String): Flow<List<Libro>>
}

/**
 * DAO para la entidad [Nota].
 */
@Dao
interface NotaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(nota: Nota)

    @Delete
    suspend fun eliminar(nota: Nota)

    @Query("SELECT * FROM notas WHERE libroId = :libroId ORDER BY id DESC")
    fun obtenerNotasPorLibro(libroId: Int): Flow<List<Nota>>
}
