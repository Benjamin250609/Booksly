package com.example.booksly.view

import com.example.booksly.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.booksly.model.Libro
import com.example.booksly.ui.theme.BookslyBotonPrincipal
import com.example.booksly.view.components.LibroPortada
import com.example.booksly.viewmodel.InicioViewModel

@Composable
fun InicioScreen(
    inicioViewModel: InicioViewModel,
    onNavigateToAddLibro: () -> Unit,
    onNavigateToLibroDetalle: (Int) -> Unit
) {
    val libros by inicioViewModel.libros.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "¡Bienvenido de vuelta!",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Un lector vive mil vidas antes de morir...",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // --- Encabezado de la Estantería ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mi Estantería",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { onNavigateToAddLibro() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BookslyBotonPrincipal,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Agregar Libro",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Añadir")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // --- Contenido de la Estantería ---
        if (libros.isEmpty()) {
            EstanteriaVaciaComposable(onNavigateToAddLibro)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 130.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
            ) {
                items(libros, key = { it.id }) { libro ->
                    LibroItemComposable(
                        libro = libro,
                        onClick = { onNavigateToLibroDetalle(libro.id) }
                    )
                }
            }
        }
    }
}

// --- COMPOSABLE PARA UN LIBRO EN LA ESTANTERÍA (MEJORADO) ---
@Composable
fun LibroItemComposable(libro: Libro, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Sombra sutil
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LibroPortada(
                portadaUrl = libro.portada,
                titulo = libro.titulo,
                modifier = Modifier
                    .height(220.dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = libro.titulo,
                style = MaterialTheme.typography.bodyMedium, // Texto más legible
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// --- COMPOSABLE PARA CUANDO NO HAY LIBROS (MEJORADO) ---
@Composable
fun EstanteriaVaciaComposable(onNavigateToAddLibro: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.LibraryBooks,
                contentDescription = "Estantería vacía",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                "Tu estantería está vacía.",
                style = MaterialTheme.typography.titleMedium
            )
            Button(
                onClick = onNavigateToAddLibro,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BookslyBotonPrincipal,
                    contentColor = Color.White
                )
            ) {
                Text("¡Añade tu primer libro!")
            }
        }
    }
}
