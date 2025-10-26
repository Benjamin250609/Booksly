package com.example.booksly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.booksly.navigation.AppNavigation
import com.example.booksly.ui.theme.BookslyTheme

/**
 * Actividad principal de la aplicación.
 * Es el punto de entrada de la interfaz de usuario.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Habilita el modo de borde a borde para que la app ocupe toda la pantalla.
        enableEdgeToEdge()
        // Establece el contenido de la actividad utilizando Jetpack Compose.
        setContent {
            // Aplica el tema personalizado de la aplicación (colores, tipografía, etc.).
            BookslyTheme {
                // Contenedor principal de la UI con el color de fondo del tema.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Carga el componente de navegación principal, que gestiona las pantallas de la app.
                    AppNavigation(app = application as BookslyApplication)
                }
            }
        }
    }
}
