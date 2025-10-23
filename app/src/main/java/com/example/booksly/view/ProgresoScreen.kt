package com.example.booksly.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn // Usaremos LazyColumn en lugar de d-grid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.booksly.R
import com.example.booksly.model.Libro
import com.example.booksly.viewmodel.EstadisticasUiState // Importar el data class
import com.example.booksly.viewmodel.ProgresoViewModel
import kotlin.math.roundToInt

@Composable
fun ProgresoScreen(
    progresoViewModel: ProgresoViewModel,
    onNavigateToLibroDetalle: (Int) -> Unit
) {
    // Observa el estado combinado del ViewModel
    val uiState by progresoViewModel.estadisticasUiState.collectAsState()

    // Usamos LazyColumn para el scroll principal
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp) // Espacio entre secciones
    ) {
        // --- Título ---
        item {
            Text(
                "Mi Progreso de Lectura",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp) // Un poco de espacio extra abajo
            )
        }

        // --- Tarjetas de Estadísticas ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp) // Espacio entre tarjetas
            ) {
                // Composable reutilizable para cada tarjeta
                TarjetaEstadisticaComposable(
                    label = "Libros Terminados",
                    value = uiState.librosFinalizadosCount.toString(),
                    modifier = Modifier.weight(1f) // Ocupa espacio equitativo
                )
                TarjetaEstadisticaComposable(
                    label = "Páginas Leídas",
                    // Formatear número con separador de miles (simple)
                    value = uiState.totalPaginasLeidas.toString().reversed().chunked(3).joinToString(".").reversed(),
                    modifier = Modifier.weight(1f)
                )
                // Podríamos añadir Páginas/Hora si tuviéramos los datos
                // TarjetaEstadisticaComposable(label = "Páginas / Hora", value = "30", modifier = Modifier.weight(1f))
            }
        }

        // --- Lista Libros en Curso ---
        item {
            ListaLibrosProgresoComposable(
                titulo = "Libros en Curso",
                libros = uiState.librosEnCurso
                // onClick = onNavigateToLibroDetalle // Pasar lambda si es clickeable
            )
        }

        // --- Separador ---
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        }

        // --- Lista Libros Finalizados ---
        item {
            ListaLibrosProgresoComposable(
                titulo = "Libros Finalizados",
                libros = uiState.librosFinalizadosList
                // onClick = onNavigateToLibroDetalle // Pasar lambda si es clickeable
            )
        }
    }
}


// --- Composables Reutilizables ---

// Similar a tu TarjetaEstadistica.jsx
@Composable
fun TarjetaEstadisticaComposable(label: String, value: String, modifier: Modifier = Modifier) {
    Card( // Usamos Card para darle elevación y bordes
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

// Similar a tu ListaLibrosProgreso.jsx
@Composable
fun ListaLibrosProgresoComposable(
    titulo: String,
    libros: List<Libro>,
    modifier: Modifier = Modifier
    // onClick: (Int) -> Unit = {} // Función de click opcional
) {
    Column(modifier = modifier) {
        Text(titulo, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

        if (libros.isEmpty()) {
            Text(
                if (titulo == "Libros en Curso") "No tienes libros en curso." else "Aún no has terminado ningún libro.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        } else {
            // Usamos Column con forEach porque las listas suelen ser cortas aquí
            // Si esperas listas muy largas, podrías usar otro LazyColumn anidado (con precaución)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                libros.forEach { libro ->
                    LibroProgresoItemComposable(
                        libro = libro
                        // modifier = Modifier.clickable { onClick(libro.id) } // Hacer clickeable
                    )
                }
            }
        }
    }
}

// Composable para cada item en las listas de progreso
@Composable
fun LibroProgresoItemComposable(libro: Libro, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Portada
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(libro.portada)
                    .crossfade(true)
                    .placeholder(R.drawable.placeholder_book)
                    .error(R.drawable.placeholder_book_error)
                    .build(),
                contentDescription = "Portada de ${libro.titulo}",
                modifier = Modifier
                    .size(width = 60.dp, height = 90.dp) // Tamaño fijo
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            // Info y Progreso
            Column(modifier = Modifier.weight(1f)) {
                Text(libro.titulo, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(libro.autor, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
                Spacer(Modifier.height(8.dp))

                // Muestra barra de progreso si está 'leyendo', o texto 'Completado' si 'finalizado'
                if (libro.estado == "leyendo" && libro.totalPaginas > 0) {
                    val progreso = libro.paginaActual.toFloat() / libro.totalPaginas.toFloat()
                    val porcentaje = (progreso * 100).roundToInt()
                    LinearProgressIndicator(
                        progress = { progreso },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(MaterialTheme.shapes.small)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Pág. ${libro.paginaActual} de ${libro.totalPaginas}",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            "$porcentaje%",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                } else if (libro.estado == "finalizado") {
                    Text(
                        "✔ Completado",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary // O un color verde
                    )
                }
            }
        }
    }
}