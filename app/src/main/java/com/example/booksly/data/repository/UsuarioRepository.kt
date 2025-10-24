package com.example.booksly.data.repository


import com.example.booksly.model.Usuario
import com.example.booksly.model.UsuarioDao
import kotlinx.coroutines.flow.Flow

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    suspend fun crearUsuario(usuario: Usuario) {
        usuarioDao.insertar(usuario)
    }

    suspend fun buscarUsuarioPorEmail(email: String): Usuario? {
        return usuarioDao.buscarPorCorreo(email)
    }

    // --- FUNCIÓN AÑADIDA ---
    // Obtiene un usuario como un Flow para observación reactiva
    fun buscarUsuarioPorEmailFlow(email: String): Flow<Usuario?> {
        return usuarioDao.buscarPorCorreoFlow(email)
    }
}
