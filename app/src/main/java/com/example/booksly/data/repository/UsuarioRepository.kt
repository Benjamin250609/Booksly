package com.example.booksly.data.repository

import com.example.booksly.model.Usuario
import com.example.booksly.model.UsuarioDao
import kotlinx.coroutines.flow.Flow

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    suspend fun getUsuarioPorEmail(email: String): Usuario? {
        return usuarioDao.buscarPorEmail(email)
    }

    suspend fun insertUsuario(usuario: Usuario) {
        usuarioDao.insertOrUpdate(usuario)
    }

    suspend fun login(email: String, clave: String): Result<Usuario> {
        val usuario = usuarioDao.buscarPorEmail(email)
        return if (usuario != null && usuario.clave == clave) {
            Result.success(usuario)
        } else {
            Result.failure(Exception("Correo o contraseña incorrectos"))
        }
    }
    
    fun getUsuarioPorEmailFlow(email: String): Flow<Usuario?> {
        return usuarioDao.buscarPorEmailFlow(email)
    }
}
