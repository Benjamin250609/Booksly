package com.example.booksly.view

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.booksly.data.util.FotoUtils
import com.example.booksly.ui.theme.BookslyBotonPrincipal
import com.example.booksly.viewmodel.PerfilUiState
import com.example.booksly.viewmodel.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    perfilViewModel: PerfilViewModel,
    onLogout: () -> Unit
) {
    val uiState by perfilViewModel.uiState.collectAsState()
    val logoutExitoso by perfilViewModel.logoutExitoso.collectAsState()

    LaunchedEffect(logoutExitoso) {
        if (logoutExitoso) {
            onLogout()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mi Perfil") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            EncabezadoPerfil(uiState = uiState)
            Spacer(Modifier.height(32.dp))
            EstadisticasPerfil(uiState = uiState)
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 16.dp))
            AccionesPerfil(viewModel = perfilViewModel)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun EncabezadoPerfil(uiState: PerfilUiState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ImagenPerfil(imagenUri = uiState.imagenUri, modifier = Modifier.size(120.dp))
        Text(uiState.nombreUsuario, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(uiState.emailUsuario, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ImagenPerfil(imagenUri: Uri?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (imagenUri != null) {
            Image(
                painter = rememberAsyncImagePainter(model = imagenUri),
                contentDescription = "Imagen de perfil",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = "Icono de perfil por defecto",
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EstadisticasPerfil(uiState: PerfilUiState) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TarjetaEstadisticaProgreso(
            label = "Libros Terminados",
            value = uiState.librosTerminados.toString(),
            icon = Icons.Outlined.CheckCircleOutline,
            modifier = Modifier.weight(1f)
        )
        TarjetaEstadisticaProgreso(
            label = "Páginas Leídas",
            value = uiState.paginasLeidas.toString().reversed().chunked(3).joinToString(".").reversed(),
            icon = Icons.Outlined.MenuBook,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AccionesPerfil(viewModel: PerfilViewModel) {
    val context = LocalContext.current

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> viewModel.onImagenSeleccionada(uri) }
    )

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success -> if (success) viewModel.onImagenSeleccionada(tempCameraUri) }
    )
    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val uri = FotoUtils.crearUriTemporal(context)
                tempCameraUri = uri
                takePictureLauncher.launch(uri)
            }
        }
    )

    fun launchCameraWithPermissionCheck() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val uri = FotoUtils.crearUriTemporal(context)
            tempCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Ajustes de la cuenta", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        Button(
            onClick = { pickImageLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = BookslyBotonPrincipal,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Outlined.Collections, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Cambiar foto desde Galería")
        }
        Button(
            onClick = { launchCameraWithPermissionCheck() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = BookslyBotonPrincipal,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Tomar nueva foto")
        }
        Button(
            onClick = { viewModel.cerrarSesion() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Outlined.Logout, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Cerrar Sesión")
        }
    }
}
