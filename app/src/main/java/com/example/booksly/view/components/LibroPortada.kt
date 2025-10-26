package com.example.booksly.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.booksly.R
import kotlin.math.abs

@Composable
fun LibroPortada(
    portadaUrl: String?,
    titulo: String,
    modifier: Modifier = Modifier
) {
    if (!portadaUrl.isNullOrBlank()) {
        // Si hay una URL, usa Coil para cargar la imagen
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(portadaUrl)
                .crossfade(true)
                .placeholder(R.drawable.placeholder_book) // Un placeholder genérico mientras carga
                .error(R.drawable.placeholder_book_error)
                .build(),
            contentDescription = "Portada de $titulo",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        // Si no hay URL, muestra el placeholder generativo
        val colorFondo = generarColorDeString(titulo)
        val inicial = titulo.firstOrNull()?.uppercaseChar() ?: ' '

        Box(
            modifier = modifier.background(colorFondo),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = inicial.toString(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}

// Función para generar un color predecible basado en un string
private fun generarColorDeString(input: String): Color {
    val hash = input.hashCode()
    val hue = abs(hash % 360).toFloat()
    return Color.hsv(hue, 0.5f, 0.8f) // Saturación y brillo fijos para colores pastel
}
