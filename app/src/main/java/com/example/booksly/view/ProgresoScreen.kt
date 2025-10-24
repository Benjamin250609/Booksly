package com.example.booksly.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.booksly.model.Libro
import com.example.booksly.view.components.LibroPortada
import com.example.booksly.viewmodel.ProgresoViewModel
import kotlin.math.roundToInt

@Composable
fun ProgresoScreen(
    progresoViewModel: ProgresoViewModel,
    onNavigateToLibroDetalle: (Int) -> Unit
) {
    val uiState by progresoViewModel.estadisticasUiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                "Mi Progreso de Lectura",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TarjetaEstadisticaProgreso(
                    label = "Libros Terminados",
                    value = uiState.librosFinalizadosCount.toString(),
                    icon = Icons.Outlined.CheckCircleOutline,
                    modifier = Modifier.weight(1f)
                )
                TarjetaEstadisticaProgreso(
                    label = "Páginas Leídas",
                    value = uiState.totalPaginasLeidas.toString().reversed().chunked(3).joinToString(".").reversed(),
                    icon = Icons.Outlined.MenuBook,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            ListaLibrosProgreso(
                titulo = "Libros en Curso",
                libros = uiState.librosEnCurso,
                onLibroClick = onNavigateToLibroDetalle
            )
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        }

        item {
            ListaLibrosProgreso(
                titulo = "Libros Finalizados",
                libros = uiState.librosFinalizadosList,
                onLibroClick = onNavigateToLibroDetalle
            )
        }
    }
}

@Composable
fun TarjetaEstadisticaProgreso(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Column {
                Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(label, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ListaLibrosProgreso(
    titulo: String,
    libros: List<Libro>,
    onLibroClick: (Int) -> Unit
) {
    Column {
        Text(titulo, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

        if (libros.isEmpty()) {
            val mensaje = if (titulo == "Libros en Curso") "No tienes libros en curso." else "Aún no has terminado ningún libro."
            EmptyState(mensaje = mensaje)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                libros.forEach { libro ->
                    LibroProgresoItem(
                        libro = libro,
                        modifier = Modifier.clickable { onLibroClick(libro.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun LibroProgresoItem(libro: Libro, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LibroPortada(
                portadaUrl = libro.portada,
                titulo = libro.titulo,
                modifier = Modifier.size(width = 60.dp, height = 90.dp).clip(MaterialTheme.shapes.medium)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(libro.titulo, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(libro.autor, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Spacer(Modifier.height(8.dp))

                if (libro.estado == "leyendo" && libro.totalPaginas > 0) {
                    val progreso = libro.paginaActual.toFloat() / libro.totalPaginas.toFloat()
                    val porcentaje = (progreso * 100).roundToInt()
                    Column {
                        LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(MaterialTheme.shapes.small))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pág. ${libro.paginaActual}", style = MaterialTheme.typography.labelSmall)
                            Text("$porcentaje%", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                } else {
                    Text("✔ Completado", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun EmptyState(mensaje: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.LibraryBooks, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        Text(mensaje, style = MaterialTheme.typography.bodyMedium)
    }
}
