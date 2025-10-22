package com.example.booksly.navigation

import com.example.booksly.data.AppContainer
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.booksly.BookslyApplication
import com.example.booksly.view.AddLibroScreen
import com.example.booksly.view.LoginScreen
import com.example.booksly.view.MainScreen
import com.example.booksly.viewmodel.LoginViewModel
import com.example.booksly.viewmodel.RegistroViewModel
import com.example.booksly.viewmodel.ViewModelFactory
import com.example.booksly.view.RegistroScreen
import com.example.booksly.viewmodel.AgregarLibroViewModel

sealed class AppScreen(val route: String) {
    object Login : AppScreen("login_screen")
    object Registro : AppScreen("registro_screen")
    object Main : AppScreen("main_screen")

    object AddLibro : AppScreen("agregar_libro_screen")
}

@Composable
fun AppNavigation(app: BookslyApplication) {
    val navController = rememberNavController()
    // Creamos una fábrica genérica para todos los ViewModels que la necesiten
    // Pasamos ambos repositorios
    val factory = ViewModelFactory(app.container.usuarioRepository, app.container.libroRepository)

    NavHost(
        navController = navController,
        startDestination = AppScreen.Login.route
    ) {
        composable(route = AppScreen.Login.route) {
            val loginViewModel: LoginViewModel = viewModel(factory = factory)
            LoginScreen(
                loginViewModel = loginViewModel,
                onLoginSuccess = {
                    // Navega a la ruta principal (Main) después del éxito
                    navController.navigate(AppScreen.Main.route) {
                        // Limpia el historial para que no se pueda volver a Login con el botón Atrás
                        popUpTo(AppScreen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegistro = {
                    navController.navigate(AppScreen.Registro.route)
                }
            )
        }

        composable(route = AppScreen.Registro.route) {
            val registroViewModel: RegistroViewModel = viewModel(factory = factory)
            RegistroScreen(
                registroViewModel = registroViewModel,
                onRegistroSuccess = {
                    navController.popBackStack() // Volver a Login después de registrarse
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }


        composable(route = AppScreen.Main.route) {
            MainScreen(app = app, rootNavController = navController)
        }

        composable(AppScreen.AddLibro.route) {
            // Crea el ViewModel para la pantalla de agregar libro
            val agregarLibroViewModel: AgregarLibroViewModel = viewModel(factory = factory)

            AddLibroScreen(
                agregarLibroViewModel = agregarLibroViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
