package com.example.booksly



import android.app.Application
import com.example.booksly.data.AppContainer
import com.example.booksly.data.AppDataContainer



class BookslyApplication : Application() {
    // La instancia del contenedor será creada una sola vez y compartida en toda la app.
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
