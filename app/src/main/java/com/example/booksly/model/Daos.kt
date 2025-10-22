import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.booksly.model.Libro
import com.example.booksly.model.Usuario
import kotlinx.coroutines.flow.Flow


@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE correo = :email LIMIT 1")
    suspend fun buscarPorCorreo(email: String): Usuario?
}


@Dao
interface LibroDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(libro: Libro)

    @Update
    suspend fun actualizar(libro: Libro)

    @Query("SELECT * FROM libros ORDER BY id DESC")
    fun obtenerLibros(): Flow<List<Libro>>

    @Query("SELECT * FROM libros WHERE id = :libroId LIMIT 1")
    fun obtenerLibroPorId(libroId: Int): Flow<Libro?>

    @Query("SELECT COUNT(*) FROM libros WHERE estado = 'finalizado'")
    fun contarLibrosFinalizados(): Flow<Int>

    @Query("SELECT COALESCE(SUM(totalPaginas), 0) FROM libros")
    fun contarPaginasLeidas(): Flow<Int>




}