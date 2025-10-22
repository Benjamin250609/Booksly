package com.example.booksly.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.booksly.viewmodel.AgregarLibroViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLibroScreen(
    agregarLibroViewModel: AgregarLibroViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by agregarLibroViewModel.uiState.collectAsState()


    LaunchedEffect(uiState.libroGuardado) {
        if (uiState.libroGuardado) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Nuevo Libro") },
                navigationIcon = {
                    // Botón para volver manualmente
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Column permite scroll si el contenido no cabe
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Padding de Scaffold
                .padding(16.dp) // Padding adicional
                .verticalScroll(rememberScrollState()), // Habilitar scroll
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Espacio entre elementos
        ) {
            OutlinedTextField(
                value = uiState.titulo,
                onValueChange = { agregarLibroViewModel.onTituloChange(it) },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.errorTitulo != null,
                supportingText = { // Muestra error si existe
                    uiState.errorTitulo?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            )

            // Campo Autor
            OutlinedTextField(
                value = uiState.autor,
                onValueChange = { agregarLibroViewModel.onAutorChange(it) },
                label = { Text("Autor") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.errorAutor != null,
                supportingText = {
                    uiState.errorAutor?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            )

            // Campo Número de Páginas
            OutlinedTextField(
                value = uiState.totalPaginas,
                onValueChange = { agregarLibroViewModel.onTotalPaginasChange(it) },
                label = { Text("Número de Páginas") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.errorTotalPaginas != null,
                supportingText = {
                    uiState.errorTotalPaginas?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Campo URL Portada (Opcional)
            OutlinedTextField(
                value = uiState.coverUrl,
                onValueChange = { agregarLibroViewModel.onCoverUrlChange(it) },
                label = { Text("URL de la Portada (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
                // No se valida la URL aquí por simplicidad
            )

            // Muestra error general si ocurre al guardar
            uiState.errorGeneral?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            // Botón Guardar
            Button(
                onClick = { agregarLibroViewModel.guardarLibro() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading // Deshabilitado mientras carga
            ) {
                if (uiState.isLoading) { // Muestra indicador si está cargando
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Guardar Libro")
                }
            }
        }
    }
}
