package com.example.booksly



import AppContainer
import AppDataContainer
import android.app.Application


class BookslyApplication : Application() {
    // La instancia del contenedor será creada una sola vez y compartida en toda la app.
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
