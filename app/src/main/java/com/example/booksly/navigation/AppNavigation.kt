package com.example.booksly.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.booksly.BookslyApplication
import com.example.booksly.view.AddLibroScreen
import com.example.booksly.view.LibroDetalleScreen
import com.example.booksly.view.LoginScreen
import com.example.booksly.view.MainScreen
import com.example.booksly.view.RegistroScreen
import com.example.booksly.viewmodel.AppViewModelProvider
import com.example.booksly.viewmodel.AgregarLibroViewModel
import com.example.booksly.viewmodel.LibroDetalleViewModel
import com.example.booksly.viewmodel.LoginViewModel
import com.example.booksly.viewmodel.RegistroViewModel
import kotlinx.coroutines.flow.first

/**
 * Define todas las pantallas (rutas) de la aplicación para una navegación segura.
 */
sealed class AppScreen(val route: String) {
    object Login : AppScreen("login_screen")
    object Registro : AppScreen("registro_screen")
    object Main : AppScreen("main_screen")

    object AddEditLibro : AppScreen("add_edit_libro_screen/{libroId}") {
        const val ARG_ID = "libroId"
        val routeWithArg = "add_edit_libro_screen/{$ARG_ID}"
        val addRoute = "add_edit_libro_screen/-1" // Ruta para añadir un nuevo libro.
        fun createRoute(libroId: Int) = "add_edit_libro_screen/$libroId"
    }

    object LibroDetalle : AppScreen("libro_detalle_screen/{libroId}") {
        fun createRoute(libroId: Int) = "libro_detalle_screen/$libroId"
    }
}

/**
 * Composable principal que gestiona la navegación de la aplicación.
 * Determina la pantalla de inicio y configura el NavHost con todas las rutas.
 */
@Composable
fun AppNavigation(app: BookslyApplication) {
    val navController = rememberNavController()
    val preferenciasRepository = app.container.preferenciasRepository

    // Estado para gestionar la carga inicial y la pantalla de destino.
    var isLoading by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf(AppScreen.Login.route) }

    // Efecto para determinar la pantalla de inicio al lanzar la app.
    LaunchedEffect(key1 = Unit) {
        // Comprueba si hay un email de usuario guardado en las preferencias.
        val email = preferenciasRepository.usuarioEmailFlow.first()
        startDestination = if (email.isNullOrBlank()) {
            // Si no hay usuario, la pantalla de inicio es el Login.
            AppScreen.Login.route
        } else {
            // Si ya hay un usuario, va directamente a la pantalla principal.
            AppScreen.Main.route
        }
        isLoading = false
    }

    if (isLoading) {
        // Muestra un indicador de carga mientras se determina la ruta de inicio.
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        // Configura el NavHost con el controlador de navegación y la pantalla de inicio.
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            // Pantalla de Login.
            composable(route = AppScreen.Login.route) {
                val loginViewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory)
                LoginScreen(
                    loginViewModel = loginViewModel,
                    onLoginSuccess = {
                        // Navega a la pantalla principal y limpia la pila de navegación para que el usuario no pueda volver al login.
                        navController.navigate(AppScreen.Main.route) {
                            popUpTo(AppScreen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegistro = {
                        navController.navigate(AppScreen.Registro.route)
                    }
                )
            }

            // Pantalla de Registro.
            composable(route = AppScreen.Registro.route) {
                val registroViewModel: RegistroViewModel = viewModel(factory = AppViewModelProvider.Factory)
                RegistroScreen(
                    registroViewModel = registroViewModel,
                    onRegistroSuccess = { navController.popBackStack() }, // Vuelve a la pantalla anterior (Login) tras el registro.
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Pantalla Principal.
            composable(route = AppScreen.Main.route) {
                MainScreen(app = app, rootNavController = navController)
            }

            // Pantalla para Añadir o Editar un Libro.
            composable(
                route = AppScreen.AddEditLibro.routeWithArg,
                arguments = listOf(navArgument(AppScreen.AddEditLibro.ARG_ID) { type = NavType.IntType; defaultValue = -1 })
            ) {
                val agregarLibroViewModel: AgregarLibroViewModel = viewModel(factory = AppViewModelProvider.Factory)
                AddLibroScreen(
                    agregarLibroViewModel = agregarLibroViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Pantalla de Detalle de un Libro.
            composable(
                route = AppScreen.LibroDetalle.route, 
                arguments = listOf(navArgument("libroId") { type = NavType.IntType })
            ) { 
                val libroDetalleViewModel: LibroDetalleViewModel = viewModel(factory = AppViewModelProvider.Factory)
                LibroDetalleScreen(
                    libroDetalleViewModel = libroDetalleViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { libroId -> navController.navigate(AppScreen.AddEditLibro.createRoute(libroId)) }
                )
            }
        }
    }
}
