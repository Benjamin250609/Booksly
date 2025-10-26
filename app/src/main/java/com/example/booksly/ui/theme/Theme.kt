package com.example.booksly.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta de colores para el tema claro, basada en tu CSS
private val LightColorScheme = lightColorScheme(
    primary = BookslyPrimario,
    secondary = BookslySecundario,
    background = BookslyFondo,
    surface = BookslyBlanco,
    onPrimary = BookslyBlanco,
    onSecondary = BookslyBlanco,
    onBackground = BookslyTextoPrincipal,
    onSurface = BookslyTextoPrincipal,
    outline = BookslyBorde,
    surfaceVariant = BookslyFondo,
    onSurfaceVariant = BookslyTextoSecundario
)

// Paleta de colores para el tema oscuro (puedes personalizarla si quieres)
private val DarkColorScheme = darkColorScheme(
    primary = BookslyPrimario,
    secondary = BookslySecundario,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF2B292C),
    onPrimary = BookslyBlanco,
    onSecondary = BookslyBlanco,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    outline = Color(0xFF938F99),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0)
)

@Composable
fun BookslyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desactivamos el color dinámico para usar siempre nuestra paleta
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
