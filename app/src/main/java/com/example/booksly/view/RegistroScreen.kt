package com.example.booksly.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.booksly.ui.theme.BookslyBotonPrincipal
import com.example.booksly.viewmodel.RegistroViewModel

/**
 * Pantalla para el registro de nuevos usuarios.
 *
 * @param registroViewModel ViewModel para la lógica de registro.
 * @param onRegistroSuccess Callback que se ejecuta al registrarse con éxito.
 * @param onNavigateBack Callback para navegar a la pantalla anterior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    registroViewModel: RegistroViewModel,
    onRegistroSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    // Recogemos el estado de la UI del ViewModel.
    val uiState by registroViewModel.uiState.collectAsState()
    // Obtenemos el gestor de foco para poder ocultar el teclado.
    val focusManager = LocalFocusManager.current

    // Cuando el registro es exitoso, se ejecuta el callback para navegar.
    LaunchedEffect(uiState.registroExitoso) {
        if (uiState.registroExitoso) {
            onRegistroSuccess()
        }
    }

    Scaffold(
        topBar = {
            // Barra de navegación superior con título y botón para volver atrás.
            TopAppBar(
                title = { Text("Crear Nueva Cuenta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Columna principal que permite el scroll.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "¡Únete a Booksly! Es rápido y sencillo.",
                style = MaterialTheme.typography.bodyLarge
            )

            // --- Campos del Formulario de Registro ---
            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = registroViewModel::onNombreChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre de Usuario") },
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                isError = uiState.nombreError != null,
                supportingText = { uiState.nombreError?.let { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = uiState.email,
                onValueChange = registroViewModel::onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Correo Electrónico") },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                isError = uiState.emailError != null,
                supportingText = { uiState.emailError?.let { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = uiState.clave,
                onValueChange = registroViewModel::onClaveChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                isError = uiState.claveError != null,
                supportingText = { uiState.claveError?.let { Text(it) } },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    registroViewModel.onRegistroClick()
                })
            )
            
            // Muestra un error general si ocurre algún problema.
            uiState.mensajeErrorGeneral?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f)) // Empuja el botón hacia la parte inferior.

            // --- Botón de Registrarse ---
            Button(
                onClick = {
                    focusManager.clearFocus()
                    registroViewModel.onRegistroClick()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !uiState.isLoading, // Se deshabilita durante la carga.
                colors = ButtonDefaults.buttonColors(
                    containerColor = BookslyBotonPrincipal,
                    contentColor = Color.White
                )
            ) {
                if (uiState.isLoading) {
                    // Muestra un indicador de progreso durante el registro.
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Crear Cuenta")
                }
            }
        }
    }
}
