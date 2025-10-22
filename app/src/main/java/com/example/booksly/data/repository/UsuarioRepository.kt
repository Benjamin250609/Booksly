package com.example.booksly.data.repository


import com.example.booksly.model.Usuario
import com.example.booksly.model.UsuarioDao

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    suspend fun crearUsuario(usuario: Usuario) {
        usuarioDao.insertar(usuario)
    }

    suspend fun buscarUsuarioPorEmail(email: String): Usuario? {
        return usuarioDao.buscarPorCorreo(email)
    }
}