package com.example.booksly.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.booksly.model.Libro
import com.example.booksly.model.LibroDetalleModel
import com.example.booksly.model.Nota
import com.example.booksly.ui.theme.BookslyBotonPrincipal
import com.example.booksly.view.components.LibroPortada
import com.example.booksly.viewmodel.LibroDetalleViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibroDetalleScreen(
    libroDetalleViewModel: LibroDetalleViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit
) {
    val uiState by libroDetalleViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.libroEliminado) {
        if (uiState.libroEliminado) {
            onNavigateBack()
        }
    }

    if (uiState.showConfirmacionEliminar) {
        ConfirmacionEliminarDialog(
            onConfirm = { libroDetalleViewModel.onConfirmarEliminar() },
            onDismiss = { libroDetalleViewModel.onDismissEliminar() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.libro?.titulo ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { uiState.libro?.id?.let { onNavigateToEdit(it) } }) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Editar Libro")
                    }
                    IconButton(onClick = { libroDetalleViewModel.onEliminarClick() }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Eliminar Libro")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.errorCarga != null -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.errorCarga}", textAlign = TextAlign.Center)
                }
            }
            uiState.libro != null -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = paddingValues,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { LibroEncabezadoComposable(libro = uiState.libro!!) }
                    item { Card(elevation = CardDefaults.cardElevation(2.dp)) { LibroEstadisticasComposable(libro = uiState.libro!!) } }
                    item { 
                        ActualizarProgresoComposable(
                            uiState = uiState,
                            onInputChange = libroDetalleViewModel::onPaginaActualInputChange,
                            onGuardarProgreso = libroDetalleViewModel::guardarProgresoPagina
                        )
                    }
                    item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }
                    
                    // --- SECCIÓN DEL DIARIO ---
                    item {
                        DiarioDeLecturaComposable(
                            uiState = uiState,
                            onNotaChange = libroDetalleViewModel::onNuevaNotaChange,
                            onGuardarNota = libroDetalleViewModel::guardarNuevaNota,
                            onEliminarNota = libroDetalleViewModel::eliminarNota
                        )
                    }
                    
                    items(uiState.notas) { nota ->
                        NotaItemComposable(nota = nota, onEliminar = { libroDetalleViewModel.eliminarNota(nota) })
                    }

                    if (uiState.notas.isEmpty()) {
                        item {
                            Text(
                                "Aún no tienes notas para este libro. ¡Añade la primera!", 
                                style = MaterialTheme.typography.bodyMedium, 
                                color = Color.Gray, 
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("No se pudo cargar la información del libro.", textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun DiarioDeLecturaComposable(
    uiState: LibroDetalleModel,
    onNotaChange: (String) -> Unit,
    onGuardarNota: () -> Unit,
    onEliminarNota: (Nota) -> Unit
) {
    val focusManager = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Diario de Lectura", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = uiState.nuevaNotaTexto,
            onValueChange = onNotaChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Escribe tus pensamientos sobre el libro...") },
            label = { Text("Nueva Nota") },
            trailingIcon = {
                IconButton(onClick = {
                    onGuardarNota()
                    focusManager.clearFocus()
                }, enabled = uiState.nuevaNotaTexto.isNotBlank()) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Guardar Nota")
                }
            },
            keyboardActions = KeyboardActions(onSend = { 
                onGuardarNota()
                focusManager.clearFocus()
            }),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)
        )
    }
}

@Composable
fun NotaItemComposable(nota: Nota, onEliminar: () -> Unit) {
    val formattedDate = SimpleDateFormat("dd MMM, yyyy, HH:mm", Locale.getDefault()).format(Date(nota.fecha))

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(nota.texto, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Text(formattedDate, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            IconButton(onClick = onEliminar, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Outlined.Delete, contentDescription = "Eliminar Nota", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}



@Composable
fun LibroEncabezadoComposable(libro: Libro) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        LibroPortada(
            portadaUrl = libro.portada,
            titulo = libro.titulo,
            modifier = Modifier
                .width(110.dp)
                .aspectRatio(2f / 3f)
                .clip(MaterialTheme.shapes.medium)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(libro.titulo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(libro.autor, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            ProgresoLibroComposable(
                paginaActual = libro.paginaActual,
                totalPaginas = libro.totalPaginas
            )
        }
    }
}

@Composable
fun ProgresoLibroComposable(paginaActual: Int, totalPaginas: Int) {
    val progreso = if (totalPaginas > 0) (paginaActual.toFloat() / totalPaginas) else 0f
    val porcentaje = (progreso * 100).roundToInt()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Progreso", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text("$porcentaje%", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progreso },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(MaterialTheme.shapes.small)
        )
    }
}

@Composable
fun LibroEstadisticasComposable(libro: Libro) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        StatItem("Páginas", "${libro.paginaActual} / ${libro.totalPaginas}")
        StatItem("Estado", libro.estado.replaceFirstChar { it.uppercase() })
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ActualizarProgresoComposable(
    uiState: LibroDetalleModel,
    onInputChange: (String) -> Unit,
    onGuardarProgreso: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Actualizar Progreso", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.paginaActualInput,
            onValueChange = onInputChange,
            label = { Text("¿En qué página vas?") },
            modifier = Modifier.fillMaxWidth(0.8f),
            singleLine = true,
            isError = uiState.errorPaginaInput != null,
            supportingText = { uiState.errorPaginaInput?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { 
                onGuardarProgreso()
                focusManager.clearFocus()
            })
        )

        Spacer(modifier = Modifier.height(4.dp))

        AnimatedVisibility(uiState.progresoGuardado, enter = fadeIn(), exit = fadeOut()) {
            Icon(Icons.Filled.Check, "Progreso Guardado", tint = MaterialTheme.colorScheme.primary)
            LaunchedEffect(uiState.progresoGuardado) { // Desaparece solo
                delay(2000)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onGuardarProgreso, 
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = BookslyBotonPrincipal,
                contentColor = Color.White
            )
        ) {
            Text("Guardar Progreso")
        }
    }
}

@Composable
fun ConfirmacionEliminarDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Eliminación") },
        text = { Text("¿Estás seguro de que quieres eliminar este libro de tu estantería? Esta acción no se puede deshacer.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
