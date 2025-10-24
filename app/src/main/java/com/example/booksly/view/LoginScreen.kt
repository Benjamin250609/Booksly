package com.example.booksly.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.booksly.ui.theme.BookslyBotonPrincipal
import com.example.booksly.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegistro: () -> Unit
) {
    val uiState by loginViewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.loginExitoso) {
        if (uiState.loginExitoso) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Título ---
        Text(
            text = "Booksly",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Tu estantería digital, siempre contigo.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // --- Formulario de Login ---
        OutlinedTextField(
            value = uiState.email,
            onValueChange = loginViewModel::onEmailChange,
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

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.contrasena,
            onValueChange = loginViewModel::onContrasenaChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
            isError = uiState.contrasenaError != null,
            supportingText = { uiState.contrasenaError?.let { Text(it) } },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { 
                focusManager.clearFocus()
                loginViewModel.onLoginClick()
            })
        )

        uiState.mensajeErrorGeneral?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Botón de Iniciar Sesión ---
        Button(
            onClick = { 
                focusManager.clearFocus()
                loginViewModel.onLoginClick() 
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = BookslyBotonPrincipal,
                contentColor = Color.White
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Iniciar Sesión")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Navegación a Registro ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿Aún no tienes cuenta?")
            TextButton(onClick = onNavigateToRegistro) {
                Text("Regístrate")
            }
        }
    }
}
