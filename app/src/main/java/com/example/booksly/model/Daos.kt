package com.example.booksly.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE correo = :email LIMIT 1")
    suspend fun buscarPorCorreo(email: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE correo = :email LIMIT 1")
    fun buscarPorCorreoFlow(email: String): Flow<Usuario?>
}


@Dao
interface LibroDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(libro: Libro)

    @Update
    suspend fun actualizar(libro: Libro)

    @Delete
    suspend fun eliminar(libro: Libro)

    @Query("SELECT * FROM libros ORDER BY id DESC")
    fun obtenerLibros(): Flow<List<Libro>>

    @Query("SELECT * FROM libros WHERE id = :libroId LIMIT 1")
    fun obtenerLibroPorId(libroId: Int): Flow<Libro?>

    @Query("SELECT COUNT(*) FROM libros WHERE estado = 'finalizado'")
    fun contarLibrosFinalizados(): Flow<Int>

    @Query("SELECT COALESCE(SUM(totalPaginas), 0) FROM libros WHERE estado = 'finalizado'")
    fun contarPaginasLeidas(): Flow<Int>

    @Query("SELECT * FROM libros WHERE titulo LIKE '%' || :termino || '%' OR autor LIKE '%' || :termino || '%' ORDER BY titulo ASC")
    fun buscarLibrosPorTermino(termino: String): Flow<List<Libro>>
}

// --- DAO PARA LAS NOTAS (NUEVO) ---
@Dao
interface NotaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(nota: Nota)

    @Delete
    suspend fun eliminar(nota: Nota)

    @Query("SELECT * FROM notas WHERE libroId = :libroId ORDER BY fecha DESC")
    fun obtenerNotasPorLibro(libroId: Int): Flow<List<Nota>>
}
