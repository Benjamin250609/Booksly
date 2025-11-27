package com.example.booksly.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "libros")
data class Libro(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Long, // ID del usuario al que pertenece el libro
    val titulo: String,
    val autor: String,
    val portada: String,
    val totalPaginas: Int,
    val paginaActual: Int,
    val estado: String
)
