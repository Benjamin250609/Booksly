package com.example.booksly.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Paleta de colores personalizada para el tema oscuro.
private val DarkColorScheme = darkColorScheme(
    primary = BookslyPrimario,
    secondary = BookslySecundario,
    tertiary = BookslySecundario,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onPrimary = BookslyTextoPrincipal,
    onSecondary = BookslyTextoPrincipal,
    onTertiary = BookslyTextoPrincipal,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5)
)

// Paleta de colores personalizada para el tema claro.
private val LightColorScheme = lightColorScheme(
    primary = BookslyPrimario,
    secondary = BookslySecundario,
    tertiary = BookslySecundario,
    background = BookslyFondo,
    surface = BookslyBlanco,
    onPrimary = BookslyBlanco,
    onSecondary = BookslyBlanco,
    onTertiary = BookslyBlanco,
    onBackground = BookslyTextoPrincipal,
    onSurface = BookslyTextoPrincipal
)

/**
 * Tema principal de la aplicación Booksly.
 *
 * @param darkTheme Indica si se debe usar el tema oscuro.
 * @param dynamicColor Habilita el color dinámico (disponible en Android 12+).
 */
@Composable
fun BookslyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Se deshabilita el color dinámico para usar siempre el tema personalizado
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // El color dinámico de Android solo se usa si se habilita explícitamente
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Efecto secundario para cambiar el color de la barra de estado según el tema.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    // Aplicamos el tema de Material Design 3.
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
