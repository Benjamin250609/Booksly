package com.example.booksly.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksly.data.remote.GoogleBooksApiService
import com.example.booksly.data.remote.VolumeItem
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.data.repository.PreferenciasRepository
import com.example.booksly.data.repository.UsuarioRepository
import com.example.booksly.model.AgregarLibroModel
import com.example.booksly.model.Libro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class AgregarLibroViewModel(
    private val libroRepository: LibroRepository,
    private val usuarioRepository: UsuarioRepository,
    private val preferenciasRepository: PreferenciasRepository,
    private val booksApiService: GoogleBooksApiService,
    savedStateHandle: SavedStateHandle,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgregarLibroModel())
    val uiState = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<VolumeItem>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val libroId: Int? = savedStateHandle["libroId"]

    init {
        if (libroId != null && libroId != -1) {
            _uiState.update { it.copy(isEditing = true) }
            viewModelScope.launch {
                val libro = libroRepository.obtenerLibroPorId(libroId).first()
                if (libro != null) {
                    _uiState.update {
                        it.copy(
                            titulo = libro.titulo,
                            autor = libro.autor,
                            totalPaginas = libro.totalPaginas.toString(),
                            portada = libro.portada,
                            libro = libro
                        )
                    }
                }
            }
        }
    }

    fun searchBooks(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
                return@launch
            }
            _isSearching.value = true
            try {
                val response = booksApiService.searchBooks(query)
                if (response.isSuccessful) {
                    _searchResults.value = response.body()?.items ?: emptyList()
                } else {
                    _searchResults.value = emptyList()
                }
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun onBookSelected(book: VolumeItem) {
        val bookInfo = book.volumeInfo
        val secureThumbnailUrl = bookInfo.imageLinks?.thumbnailUrl?.replace("http://", "https://") ?: ""

        _uiState.update {
            it.copy(
                titulo = bookInfo.title ?: "",
                autor = bookInfo.authors?.joinToString(", ") ?: "",
                totalPaginas = bookInfo.pageCount?.toString() ?: "",
                portada = secureThumbnailUrl,
                errorTitulo = null,
                errorAutor = null,
                errorTotalPaginas = null
            )
        }
        _searchResults.value = emptyList()
    }

    fun onTituloChange(valor: String) {
        _uiState.update { it.copy(titulo = valor, errorTitulo = null) }
    }

    fun onAutorChange(valor: String) {
        _uiState.update { it.copy(autor = valor, errorAutor = null) }
    }

    fun onTotalPaginasChange(valor: String) {
        if (valor.all { it.isDigit() }) {
            _uiState.update { it.copy(totalPaginas = valor, errorTotalPaginas = null) }
        }
    }

    fun onPortadaChange(uri: Uri?) {
        if (uri == null) {
            _uiState.update { it.copy(portada = "") }
            return
        }

        viewModelScope.launch {
            val imagePath = saveImageToInternalStorage(uri)
            _uiState.update { it.copy(portada = imagePath ?: "") }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = application.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val directory = File(application.cacheDir, "portadas")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, "${UUID.randomUUID()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()
            inputStream?.close()
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    fun guardarLibro() {
        val totalPaginas = _uiState.value.totalPaginas.toIntOrNull()
        if (_uiState.value.titulo.isBlank()) {
            _uiState.update { it.copy(errorTitulo = "El título no puede estar vacío") }
            return
        }
        if (_uiState.value.autor.isBlank()) {
            _uiState.update { it.copy(errorAutor = "El autor no puede estar vacío") }
            return
        }
        if (totalPaginas == null || totalPaginas <= 0) {
            _uiState.update { it.copy(errorTotalPaginas = "Número de páginas inválido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val email = preferenciasRepository.usuarioEmailFlow.first()
                val usuario = usuarioRepository.getUsuarioPorEmail(email ?: "")

                if (usuario == null) {
                    _uiState.update { it.copy(errorGeneral = "Error: No se pudo encontrar el usuario actual") }
                    return@launch
                }

                val libroActual = _uiState.value.libro
                val nuevoLibro = Libro(
                    id = if (_uiState.value.isEditing) libroId!! else 0,
                    userId = usuario.id.toLong(),
                    titulo = _uiState.value.titulo,
                    autor = _uiState.value.autor,
                    portada = _uiState.value.portada,
                    totalPaginas = totalPaginas,
                    paginaActual = if (_uiState.value.isEditing) libroActual?.paginaActual ?: 0 else 0,
                    estado = if (_uiState.value.isEditing) libroActual?.estado ?: "leyendo" else "leyendo"
                )

                if (_uiState.value.isEditing) {
                    libroRepository.actualizarLibro(nuevoLibro)
                } else {
                    libroRepository.agregarLibro(nuevoLibro)
                }

                _uiState.update { it.copy(libroGuardado = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorGeneral = "Error al guardar el libro") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}