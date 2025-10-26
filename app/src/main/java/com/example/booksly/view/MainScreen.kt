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

/**
 * Sealed class que define las pantallas principales accesibles desde la barra de navegación inferior.
 */
sealed class MainScreenItem(val route: String, val icon: ImageVector, val title: String) {
    object Inicio : MainScreenItem("inicio", Icons.Filled.Home, "Inicio")
    object Buscar : MainScreenItem("buscar", Icons.Filled.Search, "Buscar")
    object Progreso : MainScreenItem("progreso", Icons.Filled.Book, "Progreso")
    object Perfil : MainScreenItem("perfil", Icons.Filled.Person, "Perfil")
}

// Lista de los items para la barra de navegación.
private val botonNavItems = listOf(
    MainScreenItem.Inicio,
    MainScreenItem.Buscar,
    MainScreenItem.Progreso,
    MainScreenItem.Perfil
)

/**
 * Composable que representa la pantalla principal de la aplicación.
 * Contiene la navegación anidada para las diferentes pestañas (Inicio, Buscar, etc.).
 *
 * @param app La instancia de la aplicación para la inyección de dependencias.
 * @param rootNavController El controlador de navegación principal de la app para navegar a pantallas fuera de este scope.
 */
@Composable
fun MainScreen(app: BookslyApplication, rootNavController: NavHostController) {
    // Controlador de navegación específico para las pestañas de MainScreen.
    val navController = rememberNavController()

    Scaffold(
        // Barra de navegación inferior.
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // Se crea un item por cada pantalla en la lista.
                botonNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        // El item se marca como seleccionado si su ruta coincide con la de la pantalla actual.
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Vuelve al inicio del grafo de navegación para evitar acumular pantallas.
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Evita crear una nueva instancia de la pantalla si ya está en la cima.
                                launchSingleTop = true
                                // Restaura el estado de la pantalla al volver a ella.
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // Host de navegación para el contenido de las pestañas.
        NavHost(
            navController = navController,
            startDestination = MainScreenItem.Inicio.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MainScreenItem.Inicio.route) {
                val inicioViewModel: InicioViewModel = viewModel(factory = AppViewModelProvider.Factory)
                InicioScreen(
                    inicioViewModel = inicioViewModel,
                    // Se usa el rootNavController para navegar a pantallas de nivel superior.
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
            composable(MainScreenItem.Perfil.route) {
                val perfilViewModel: PerfilViewModel = viewModel(factory = AppViewModelProvider.Factory)
                PerfilScreen(
                    perfilViewModel = perfilViewModel,
                    onLogout = {
                        // Al cerrar sesión, se navega a la pantalla de Login y se limpia toda la pila de navegación anterior.
                        rootNavController.navigate(AppScreen.Login.route) {
                            popUpTo(AppScreen.Main.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
