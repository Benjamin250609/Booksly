package com.example.booksly.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Define la entidad [Nota] para la tabla "notas" en la base de datos de Room.
 * Cada nota está asociada a un [Libro] a través de una clave foránea.
 */
@Entity(
    tableName = "notas",
    // Define la relación con la tabla de libros.
    foreignKeys = [ForeignKey(
        entity = Libro::class, // La entidad padre es Libro.
        parentColumns = ["id"], // La columna de referencia en la tabla de libros es "id".
        childColumns = ["libroId"], // La columna en esta tabla que establece la relación es "libroId".
        onDelete = ForeignKey.CASCADE // Indica que si se elimina un libro, todas sus notas asociadas también se eliminarán.
    )]
)
data class Nota(
    // Clave primaria autogenerada para la nota.
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // ID del libro al que pertenece esta nota. Es la clave foránea.
    val libroId: Int,
    // Contenido de la nota.
    val texto: String,
    // Fecha de creación de la nota, guardada como un timestamp de Unix.
    val fecha: Long = System.currentTimeMillis()
)
