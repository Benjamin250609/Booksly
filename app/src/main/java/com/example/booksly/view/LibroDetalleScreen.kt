package com.example.booksly.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.example.booksly.viewmodel.LibroDetalleViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.booksly.model.Libro
import com.example.booksly.R
import com.example.booksly.model.LibroDetalleModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibroDetalleScreen(
    libroDetalleViewModel: LibroDetalleViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by libroDetalleViewModel.uiState.collectAsState()
    val libro = uiState.libro

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(libro?.titulo ?: "Detalle del Libro") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            // Estado de Carga
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            // Estado de Error al Cargar
            uiState.errorCarga != null -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.errorCarga}")
                }
            }
            // Libro Cargado Exitosamente
            libro != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    LibroEncabezadoComposable(libro = libro)
                    Spacer(modifier = Modifier.height(24.dp))
                    LibroEstadisticasComposable(libro = libro)
                    Spacer(modifier = Modifier.height(24.dp))
                    ActualizarProgresoComposable(
                        uiState = uiState,
                        onInputChange = libroDetalleViewModel::onPaginaActualInputChange,
                        onGuardarProgreso = libroDetalleViewModel::guardarProgresoPagina,
                        onMarcarTerminado = libroDetalleViewModel::marcarComoTerminado
                    )
                }
            }

            else -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("No se pudo cargar la información del libro.")
                }
            }
        }
    }
}

// Composable para el encabezado (Portada, Título, Autor, Progreso)
@Composable
fun LibroEncabezadoComposable(libro: Libro) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top // Alinea arriba para textos largos
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(libro.portada)
                .crossfade(true)
                .placeholder(R.drawable.placeholder_book)
                .error(R.drawable.placeholder_book_error)
                .build(),
            contentDescription = "Portada de ${libro.titulo}",
            modifier = Modifier
                .width(100.dp) // Ancho un poco menor
                .aspectRatio(2f / 3f) // Proporción común de libro
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) { // Columna ocupa espacio restante
            Text(libro.titulo, style = MaterialTheme.typography.headlineSmall)
            Text(libro.autor, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            ProgresoLibroComposable(
                paginaActual = libro.paginaActual,
                totalPaginas = libro.totalPaginas
            )
        }
    }
}

// Composable para la barra de progreso y porcentaje
@Composable
fun ProgresoLibroComposable(paginaActual: Int, totalPaginas: Int) {
    val progreso = if (totalPaginas > 0) (paginaActual.toFloat() / totalPaginas.toFloat()) else 0f
    val porcentaje = (progreso * 100).roundToInt()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Progreso", style = MaterialTheme.typography.labelMedium)
            Text("$porcentaje%", style = MaterialTheme.typography.labelMedium)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progreso }, // Necesita un Float
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(MaterialTheme.shapes.small) // Barra más gruesa y redondeada
        )
    }
}


// Composable para las estadísticas (Páginas, Tiempo - simplificado)
@Composable
fun LibroEstadisticasComposable(libro: Libro) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround // Distribuye espacio
    ) {
        StatItem("Páginas Leídas", "${libro.paginaActual} / ${libro.totalPaginas}")
        StatItem("Estado", libro.estado.replaceFirstChar { it.uppercase() })
    }
}

// Composable para un item de estadística individual
@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}


// Composable para la sección de actualizar progreso
@Composable
fun ActualizarProgresoComposable(
    uiState: LibroDetalleModel,
    onInputChange: (String) -> Unit,
    onGuardarProgreso: () -> Unit,
    onMarcarTerminado: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Registrar Sesión", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.paginaActualInput,
            onValueChange = onInputChange,
            label = { Text("¿En qué página vas?") },
            modifier = Modifier.fillMaxWidth(0.7f), // No tan ancho
            singleLine = true,
            isError = uiState.errorPaginaInput != null,
            supportingText = {
                uiState.errorPaginaInput?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            // Botón pequeño al final para guardar
            trailingIcon = {
                Button(onClick = onGuardarProgreso, Modifier.height(40.dp)) { Text("Ok") }
            }
        )

        // Mostrar confirmación si se guardó
        if (uiState.progresoGuardado) {
            Text("¡Progreso guardado!", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
        }


        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onMarcarTerminado, modifier = Modifier.fillMaxWidth()) {
            Text("Marcar como Terminado")
        }
    }
}