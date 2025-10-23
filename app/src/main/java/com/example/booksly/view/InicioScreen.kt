package com.example.booksly.view

import com.example.booksly.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.booksly.model.Libro
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
            text = "\"Un lector vive mil vidas antes de morir...\"",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )



        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mi Estantería",
                style = MaterialTheme.typography.titleLarge
            )

            Button(onClick = { onNavigateToAddLibro() }) {
                Icon(
                    Icons.Filled.AddCircle,
                    contentDescription = "Agregar Libro",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Agregar Libro")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))



        if (libros.isEmpty()) {
            Text(
                "Tu estantería está vacía. ¡Añade tu primer libro!",
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
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


@Composable
fun LibroItemComposable(libro: Libro, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(libro.portada)
                .crossfade(true)
                .placeholder(R.drawable.placeholder_book)
                .error(R.drawable.placeholder_book_error)
                .build(),
            contentDescription = "Portada de ${libro.titulo}",
            modifier = Modifier
                .height(180.dp)
                .fillMaxWidth(),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = libro.titulo,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            minLines = 2
        )
    }
}