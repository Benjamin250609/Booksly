package com.example.booksly.view


import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
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
import androidx.navigation.compose.rememberNavController
import com.example.booksly.BookslyApplication
import com.example.booksly.viewmodel.ViewModelFactory
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.booksly.navigation.AppScreen
import com.example.booksly.viewmodel.InicioViewModel



sealed class MainScreenItem(val route: String, val icon: ImageVector,val title:String ) {
    object Inicio : MainScreenItem("inicio", Icons.Filled.Home, "Inicio")
    object Buscar : MainScreenItem("buscar", Icons.Filled.Search, "Buscar")
    object Feed : MainScreenItem("feed", Icons.Filled.List, "Diario")
    object Calendario : MainScreenItem("calendario", Icons.Filled.CalendarMonth, "Metas")
    object Progreso : MainScreenItem("progreso", Icons.Filled.Book, "Progreso")
}

val botonNavItems = listOf(
    MainScreenItem.Inicio,
    MainScreenItem.Buscar,
    MainScreenItem.Feed,
    MainScreenItem.Calendario,
    MainScreenItem.Progreso
)


@Composable
fun MainScreen(app: BookslyApplication,rootNavController: NavHostController) {
    val navController = rememberNavController()
    val factory = ViewModelFactory(app.container.usuarioRepository, app.container.libroRepository)

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
                val inicioViewModel: InicioViewModel = viewModel(factory = factory)
                InicioScreen(
                    inicioViewModel = inicioViewModel,
                    onNavigateToAddLibro = {rootNavController.navigate(AppScreen.AddLibro.route)},
                )
            }
            composable(MainScreenItem.Buscar.route) { Text("Pantalla de Búsqueda Personal") }
            composable(MainScreenItem.Feed.route) { Text("Pantalla de Diario de Lectura") }
            composable(MainScreenItem.Calendario.route) { Text("Pantalla de Metas de Lectura") }
            composable(MainScreenItem.Progreso.route) { Text("Pantalla de Estadísticas") }
        }
    }
}

