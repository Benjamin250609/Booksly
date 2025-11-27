package com.example.booksly.view

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.booksly.data.remote.VolumeItem
import com.example.booksly.ui.theme.BookslyBotonPrincipal
import com.example.booksly.viewmodel.AgregarLibroViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLibroScreen(
    agregarLibroViewModel: AgregarLibroViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by agregarLibroViewModel.uiState.collectAsState()
    val searchResults by agregarLibroViewModel.searchResults.collectAsState()
    val isSearching by agregarLibroViewModel.isSearching.collectAsState()

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(uiState.libroGuardado) {
        if (uiState.libroGuardado) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "Editar Libro" else "Agregar Nuevo Libro") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!uiState.isEditing) {
                Text(
                    "Busca un libro para autocompletar o ingresa los datos manualmente.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                // --- Barra de búsqueda ---
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        agregarLibroViewModel.searchBooks(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Buscar por título o ISBN") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                agregarLibroViewModel.searchBooks("") // Limpia la búsqueda
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                )

                // --- Resultados de la búsqueda ---
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                } else if (searchResults.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Resultados de la búsqueda:", style = MaterialTheme.typography.titleMedium)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp) // Altura fija para la lista de resultados
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    ) {
                        items(searchResults) { book ->
                            SearchResultItem(book = book, onBookSelected = {
                                agregarLibroViewModel.onBookSelected(it)
                                searchQuery = "" // Limpia la query de búsqueda
                                focusManager.clearFocus()
                            })
                            Divider()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))
            }


            // --- Selector de Portada ---
            PortadaSelector(
                portada = uiState.portada,
                onPortadaChange = agregarLibroViewModel::onPortadaChange,
                onPermissionDenied = {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "El permiso de la cámara es necesario para tomar una foto.",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )

            // --- Campos del Formulario ---
            OutlinedTextField(
                value = uiState.titulo,
                onValueChange = agregarLibroViewModel::onTituloChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Título del libro") },
                leadingIcon = { Icon(Icons.Outlined.DriveFileRenameOutline, contentDescription = null) },
                isError = uiState.errorTitulo != null,
                supportingText = { uiState.errorTitulo?.let { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = uiState.autor,
                onValueChange = agregarLibroViewModel::onAutorChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Autor") },
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                isError = uiState.errorAutor != null,
                supportingText = { uiState.errorAutor?.let { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = uiState.totalPaginas,
                onValueChange = agregarLibroViewModel::onTotalPaginasChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Número de Páginas") },
                leadingIcon = { Icon(Icons.Outlined.Numbers, contentDescription = null) },
                isError = uiState.errorTotalPaginas != null,
                supportingText = { uiState.errorTotalPaginas?.let { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Spacer(modifier = Modifier.weight(1f, fill = false))

            // --- Botón de Guardar ---
            Button(
                onClick = {
                    focusManager.clearFocus()
                    agregarLibroViewModel.guardarLibro()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = BookslyBotonPrincipal, contentColor = Color.White)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (uiState.isEditing) "Guardar Cambios" else "Guardar Libro")
                }
            }

            uiState.errorGeneral?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun SearchResultItem(book: VolumeItem, onBookSelected: (VolumeItem) -> Unit) {
    ListItem(
        headlineContent = { Text(book.volumeInfo.title ?: "Sin título") },
        supportingContent = { Text(book.volumeInfo.authors?.joinToString(", ") ?: "Autor desconocido") },
        leadingContent = {
            AsyncImage(
                model = book.volumeInfo.imageLinks?.thumbnailUrl?.replace("http://", "https://"),
                contentDescription = "Portada",
                modifier = Modifier
                    .width(40.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
        },
        modifier = Modifier.clickable { onBookSelected(book) }
    )
}


@Composable
fun PortadaSelector(portada: String, onPortadaChange: (Uri?) -> Unit, onPermissionDenied: () -> Unit) {
    val context = LocalContext.current
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { onPortadaChange(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempImageUri?.let { onPortadaChange(it) }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val newUri = createTempImageFile(context)
            tempImageUri = newUri
            cameraLauncher.launch(newUri)
        } else {
            onPermissionDenied()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // --- Vista Previa de la Portada ---
        Box(
            modifier = Modifier
                .height(220.dp)
                .width(150.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (portada.isNotBlank()) {
                AsyncImage(
                    model = portada,
                    contentDescription = "Portada del libro",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Botón para eliminar la portada
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { onPortadaChange(null) }
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar portada",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = "Sin portada",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- Botones de Selección ---
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Botón de Galería
            Button(
                onClick = { galleryLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Galería")
            }
            // Botón de Cámara
            Button(
                onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Filled.AddAPhoto, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Cámara")
            }
        }
    }
}

private fun createTempImageFile(context: Context): Uri {
    val storageDir: File? = context.getExternalFilesDir("Pictures")
    val file = File.createTempFile("JPEG_${System.currentTimeMillis()}_", ".jpg", storageDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}
