package com.example.booksly.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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

sealed class AppScreen(val route: String) {
    object Login : AppScreen("login_screen")
    object Registro : AppScreen("registro_screen")
    object Main : AppScreen("main_screen")

    object AddEditLibro : AppScreen("add_edit_libro_screen/{libroId}") {
        const val ARG_ID = "libroId"
        val routeWithArg = "add_edit_libro_screen/{$ARG_ID}"
        val addRoute = "add_edit_libro_screen/-1"
        fun createRoute(libroId: Int) = "add_edit_libro_screen/$libroId"
    }

    object LibroDetalle : AppScreen("libro_detalle_screen/{libroId}") {
        fun createRoute(libroId: Int) = "libro_detalle_screen/$libroId"
    }
}

@Composable
fun AppNavigation(app: BookslyApplication) {
    val navController = rememberNavController()
    val preferenciasRepository = app.container.preferenciasRepository

    var isLoading by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf(AppScreen.Login.route) }

    LaunchedEffect(key1 = Unit) {
        val email = preferenciasRepository.usuarioEmailFlow.first()
        startDestination = if (email.isNullOrBlank()) {
            AppScreen.Login.route
        } else {
            AppScreen.Main.route
        }
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        NavHost(
            navController = navController,
            startDestination = startDestination
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
                    onRegistroSuccess = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = AppScreen.Main.route) {
                MainScreen(app = app, rootNavController = navController)
            }

            composable(
                route = AppScreen.AddEditLibro.routeWithArg,
                arguments = listOf(navArgument(AppScreen.AddEditLibro.ARG_ID) {
                    type = NavType.IntType
                    defaultValue = -1
                })
            ) {
                val agregarLibroViewModel: AgregarLibroViewModel = viewModel(factory = AppViewModelProvider.Factory)
                AddLibroScreen(
                    agregarLibroViewModel = agregarLibroViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

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
