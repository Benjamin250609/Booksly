package com.example.booksly.data.repository


import com.example.booksly.model.Usuario
import com.example.booksly.model.UsuarioDao
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio que gestiona las operaciones de datos para la entidad [Usuario].
 * Abstrae el acceso a la fuente de datos (en este caso, Room) y proporciona
 * que el ViewModels interactúen con los datos de los usuarios.
 */
class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    /**
     * Inserta un nuevo usuario en la base de datos.
     * Es una función de suspensión, por lo que debe llamarse desde una corrutina.
     *
     * @param usuario El objeto [Usuario] que se va a insertar.
     */
    suspend fun crearUsuario(usuario: Usuario) {
        usuarioDao.insertar(usuario)
    }

    /**
     * Busca un usuario por su dirección de correo electrónico.
     * Devuelve el objeto [Usuario] si se encuentra, o null en caso contrario.
     * Es una función de suspensión.
     *
     * @param email El correo electrónico del usuario a buscar.
     */
    suspend fun buscarUsuarioPorEmail(email: String): Usuario? {
        return usuarioDao.buscarPorCorreo(email)
    }

    /**
     * Busca un usuario por su email y devuelve el resultado como un [Flow].
     * Esto permite a la UI observar los cambios en los datos del usuario de forma reactiva.
     * Si los datos del usuario cambian en la base de datos, el Flow emitirá el nuevo objeto [Usuario].
     *
     * @param email El correo electrónico del usuario a observar.
     */
    fun buscarUsuarioPorEmailFlow(email: String): Flow<Usuario?> {
        return usuarioDao.buscarPorCorreoFlow(email)
    }
}
