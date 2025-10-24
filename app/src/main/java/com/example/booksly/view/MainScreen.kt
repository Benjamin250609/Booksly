package com.example.booksly.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.booksly.BookslyApplication
import com.example.booksly.navigation.AppScreen
import com.example.booksly.viewmodel.AppViewModelProvider
import com.example.booksly.viewmodel.BuscarViewModel
import com.example.booksly.viewmodel.InicioViewModel
import com.example.booksly.viewmodel.PerfilViewModel
import com.example.booksly.viewmodel.ProgresoViewModel


sealed class MainScreenItem(val route: String, val icon: ImageVector, val title: String) {
    object Inicio : MainScreenItem("inicio", Icons.Filled.Home, "Inicio")
    object Buscar : MainScreenItem("buscar", Icons.Filled.Search, "Buscar")
    object Progreso : MainScreenItem("progreso", Icons.Filled.Book, "Progreso")
    object Perfil : MainScreenItem("perfil", Icons.Filled.Person, "Perfil")
}

val botonNavItems = listOf(
    MainScreenItem.Inicio,
    MainScreenItem.Buscar,
    MainScreenItem.Progreso,
    MainScreenItem.Perfil
)


@Composable
fun MainScreen(app: BookslyApplication, rootNavController: NavHostController) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                botonNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainScreenItem.Inicio.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MainScreenItem.Inicio.route) {
                val inicioViewModel: InicioViewModel = viewModel(factory = AppViewModelProvider.Factory)
                InicioScreen(
                    inicioViewModel = inicioViewModel,
                    onNavigateToAddLibro = { rootNavController.navigate(AppScreen.AddEditLibro.addRoute) },
                    onNavigateToLibroDetalle = { libroId ->
                        rootNavController.navigate(AppScreen.LibroDetalle.createRoute(libroId))
                    }
                )
            }
            composable(MainScreenItem.Buscar.route) {
                val buscarViewModel: BuscarViewModel = viewModel(factory = AppViewModelProvider.Factory)
                BuscarScreen(
                    buscarViewModel = buscarViewModel,
                    onNavigateToLibroDetalle = { libroId ->
                        rootNavController.navigate(AppScreen.LibroDetalle.createRoute(libroId))
                    }
                )
            }

            composable(MainScreenItem.Progreso.route) {
                val progresoViewModel: ProgresoViewModel = viewModel(factory = AppViewModelProvider.Factory)
                ProgresoScreen(
                    progresoViewModel = progresoViewModel,
                    onNavigateToLibroDetalle = { libroId ->
                        rootNavController.navigate(AppScreen.LibroDetalle.createRoute(libroId))
                    }
                )
            }

            // --- PANTALLA DE PERFIL (CON NAVEGACIÓN DE LOGOUT) ---
            composable(MainScreenItem.Perfil.route) {
                val perfilViewModel: PerfilViewModel = viewModel(factory = AppViewModelProvider.Factory)
                PerfilScreen(
                    perfilViewModel = perfilViewModel,
                    onLogout = {

                        rootNavController.navigate(AppScreen.Login.route) {
                            popUpTo(AppScreen.Main.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
