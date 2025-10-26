package com.example.booksly.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Define la tabla "usuarios" en la base de datos de Room.
@Entity(tableName = "usuarios")
// Data class para representar a un usuario en la aplicación.
data class Usuario(
    // Clave primaria autogenerada para identificar de forma única a cada usuario.
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // Nombre del usuario.
    val nombre: String,
    // Email del usuario, usado para el inicio de sesión.
    val email: String,
    // Contraseña del usuario.
    val clave: String
)
