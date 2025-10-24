package com.example.booksly.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "notas",
    foreignKeys = [ForeignKey(
        entity = Libro::class,
        parentColumns = ["id"],
        childColumns = ["libroId"],
        onDelete = ForeignKey.CASCADE // Si se borra un libro, se borran sus notas
    )]
)
data class Nota(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val libroId: Int,
    val texto: String,
    val fecha: Long = System.currentTimeMillis()
)
