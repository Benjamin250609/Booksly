package com.example.booksly.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Usuario::class, Libro::class, Nota::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun libroDao(): LibroDao
    abstract fun notaDao(): NotaDao // <-- AÑADIDO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "booksly_database.db"
                )
                .fallbackToDestructiveMigration() // <-- AÑADIDO para manejar cambios de versión
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
