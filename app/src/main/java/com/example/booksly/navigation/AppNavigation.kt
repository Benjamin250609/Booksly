package com.example.booksly.navigation

import com.example.booksly.data.AppContainer
import androidx.compose.runtime.Composable
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
import com.example.booksly.viewmodel.LoginViewModel
import com.example.booksly.viewmodel.RegistroViewModel
import com.example.booksly.view.RegistroScreen
import com.example.booksly.viewmodel.AgregarLibroViewModel
import com.example.booksly.viewmodel.AppViewModelProvider
import com.example.booksly.viewmodel.LibroDetalleViewModel

sealed class AppScreen(val route: String) {
    object Login : AppScreen("login_screen")
    object Registro : AppScreen("registro_screen")
    object Main : AppScreen("main_screen")

    object AddLibro : AppScreen("agregar_libro_screen")

    object LibroDetalle : AppScreen("libro_detalle_screen/{libroId}") {
        fun createRoute(libroId: Int) = "libro_detalle_screen/$libroId"
    }
}

@Composable
fun AppNavigation(app: BookslyApplication) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppScreen.Login.route
    ) {
        composable(route = AppScreen.Login.route) {
            val loginViewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory)
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
            val registroViewModel: RegistroViewModel = viewModel(factory = AppViewModelProvider.Factory)
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
            val agregarLibroViewModel: AgregarLibroViewModel = viewModel(factory = AppViewModelProvider.Factory)

            AddLibroScreen(
                agregarLibroViewModel = agregarLibroViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(route = AppScreen.LibroDetalle.route, arguments = listOf(navArgument("libroId") { type = NavType.IntType })
        ) { backStackEntry ->
            val libroDetalleViewModel: LibroDetalleViewModel = viewModel(factory = AppViewModelProvider.Factory)
            LibroDetalleScreen(
                libroDetalleViewModel = libroDetalleViewModel,
                onNavigateBack = { navController.popBackStack() }
                )
            }

    }
}
