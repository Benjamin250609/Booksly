import android.content.Context
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.data.repository.UsuarioRepository


interface AppContainer {
    val usuarioRepository: UsuarioRepository
    val libroRepository: LibroRepository
}

// Implementación concreta del contenedor de dependencias
class AppDataContainer(private val context: Context) : AppContainer {

    private val appDatabase : AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }
    override val usuarioRepository: UsuarioRepository by lazy {
        UsuarioRepository(appDatabase.usuarioDao())
    }
    override val libroRepository: LibroRepository by lazy {
        LibroRepository(appDatabase.libroDao())
    }

}
