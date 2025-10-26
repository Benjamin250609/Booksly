package com.example.booksly

import android.app.Application
import com.example.booksly.data.AppContainer
import com.example.booksly.data.AppDataContainer

/**
 * Clase Application personalizada para Booksly.
 * Se utiliza para inicializar y mantener una instancia única del contenedor de dependencias (AppContainer)
 * que estará disponible en toda la aplicación.
 */
class BookslyApplication : Application() {
    /**
     * Contenedor de dependencias de la aplicación.
     * Se inicializa de forma diferida  en el método onCreate.
     */
    lateinit var container: AppContainer

    /**
     * Se llama cuando la aplicación se inicia.
     * Aquí es donde creamos la instancia del contenedor de dependencias.
     */
    override fun onCreate() {
        super.onCreate()
        // Inicializa el contenedor de dependencias con el contexto de la aplicación.
        container = AppDataContainer(this)
    }
}
