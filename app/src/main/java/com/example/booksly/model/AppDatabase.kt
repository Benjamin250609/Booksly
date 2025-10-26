package com.example.booksly.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Define la base de datos de Room, sus entidades y la versión.
@Database(entities = [Usuario::class, Libro::class, Nota::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    // Métodos abstractos para obtener los DAO de cada entidad.
    abstract fun usuarioDao(): UsuarioDao
    abstract fun libroDao(): LibroDao
    abstract fun notaDao(): NotaDao

    // Singleton para asegurar que solo haya una instancia de la base de datos.
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Si la instancia no es nula, la devuelve; si no, la crea.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "booksly_database.db"
                )
                // Permite a Room recrear las tablas de la base de datos si no se proporcionan migraciones.
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                // Devuelve la instancia recién creada.
                instance
            }
        }
    }
}
