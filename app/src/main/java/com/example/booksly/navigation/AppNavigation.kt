package com.example.booksly.navigation

import AppContainer
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.booksly.BookslyApplication
import com.example.booksly.view.LoginScreen
import com.example.booksly.viewmodel.LoginViewModel
import com.example.booksly.viewmodel.RegistroViewModel
import com.example.booksly.viewmodel.ViewModelFactory
import com.example.booksly.view.RegistroScreen

sealed class AppScreen(val route: String) {
    object Login : AppScreen("login_screen")
    object Registro : AppScreen("registro_screen")
    object Main : AppScreen("main_screen")
}

@Composable
fun AppNavigation(app: BookslyApplication) { // <-- PARÁMETRO AÑADIDO
    val navController = rememberNavController()
    val factory = ViewModelFactory(app.container.usuarioRepository)

    NavHost(
        navController = navController,
        startDestination = AppScreen.Login.route
    ) {
        composable(route = AppScreen.Login.route) {
            val loginViewModel: LoginViewModel = viewModel(factory = factory)
            LoginScreen(
                loginViewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(AppScreen.Main.route) {
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

            androidx.compose.material3.Text("¡Login Exitoso! Bienvenido a la App.")
        }
    }
}