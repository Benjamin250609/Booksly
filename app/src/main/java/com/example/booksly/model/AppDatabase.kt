package com.example.booksly.model


import com.example.booksly.model.LibroDao
import com.example.booksly.model.UsuarioDao
import androidx.room.Database
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase



@Database(entities = [Usuario::class, Libro::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun libroDao(): LibroDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                            context.applicationContext,
                    AppDatabase::class.java,
                    "booksly_database.db").build()
                INSTANCE = instance
                instance
            }
        }
    }
}
