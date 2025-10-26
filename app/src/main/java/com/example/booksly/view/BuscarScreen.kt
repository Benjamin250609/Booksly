package com.example.booksly.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booksly.viewmodel.BuscarViewModel

/**
 * Pantalla que permite al usuario buscar libros en su estantería.
 *
 * @param buscarViewModel ViewModel que gestiona la lógica de búsqueda.
 * @param onNavigateToLibroDetalle Callback para navegar a la pantalla de detalle de un libro.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscarScreen(
    buscarViewModel: BuscarViewModel,
    onNavigateToLibroDetalle: (Int) -> Unit
) {
    // Observa el estado de la UI desde el ViewModel.
    val uiState by buscarViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Buscar en mi Estantería") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- Barra de Búsqueda ---
            OutlinedTextField(
                value = uiState.terminoBusqueda,
                onValueChange = buscarViewModel::onTerminoBusquedaChange,
                label = { Text("Título o autor...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Buscar") },
                // Icono para limpiar el campo de búsqueda, aparece solo si hay texto.
                trailingIcon = {
                    if (uiState.terminoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { buscarViewModel.onTerminoBusquedaChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Área de Resultados ---
            // El contenido cambia según el estado de la búsqueda.
            when {
                // Si el término de búsqueda está vacío, muestra un mensaje inicial.
                uiState.terminoBusqueda.isBlank() -> {
                    EmptyState(mensaje = "Escribe algo para encontrar libros en tu estantería.")
                }
                // Si se buscó pero no hubo resultados, muestra un mensaje informativo.
                uiState.sinResultados -> {
                    EmptyState(mensaje = "No se encontraron libros para \"${uiState.terminoBusqueda}\".")
                }
                // Si hay resultados, muestra la lista.
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(uiState.resultados, key = { it.id }) { libro ->
                            // Se reutiliza el Composable LibroProgresoItem para mostrar cada resultado.
                            LibroProgresoItem(
                                libro = libro,
                                modifier = Modifier.clickable { onNavigateToLibroDetalle(libro.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
