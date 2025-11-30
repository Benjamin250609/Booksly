
package com.example.booksly.viewmodel

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.example.booksly.data.remote.BookSearchResponse
import com.example.booksly.data.remote.GoogleBooksApiService
import com.example.booksly.data.remote.ImageLinks
import com.example.booksly.data.remote.VolumeInfo
import com.example.booksly.data.remote.VolumeItem
import com.example.booksly.data.repository.LibroRepository
import com.example.booksly.data.repository.PreferenciasRepository
import com.example.booksly.data.repository.UsuarioRepository
import com.example.booksly.model.Libro
import com.example.booksly.model.Usuario
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import retrofit2.Response
import java.time.LocalDate

@ExperimentalCoroutinesApi
class AgregarLibroViewModelTest {

    private val libroRepository: LibroRepository = mockk(relaxed = true)
    private val usuarioRepository: UsuarioRepository = mockk(relaxed = true)
    private val preferenciasRepository: PreferenciasRepository = mockk(relaxed = true)
    private val application: Application = mockk(relaxed = true)
    private val mockApiService: GoogleBooksApiService = mockk()
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: AgregarLibroViewModel

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        savedStateHandle = SavedStateHandle()
    }

    private fun initializeViewModel() {
        viewModel = AgregarLibroViewModel(
            libroRepository,
            usuarioRepository,
            preferenciasRepository,
            mockApiService,
            savedStateHandle,
            application
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("El estado inicial del ViewModel es correcto")
    fun `estado inicial correcto`() {
        initializeViewModel()
        viewModel.uiState.value.isEditing shouldBe false
        viewModel.searchResults.value.isEmpty() shouldBe true
    }

    @Test
    @DisplayName("La búsqueda de libros actualiza los resultados")
    fun `la búsqueda actualiza los resultados`() = runTest {
        val query = "Kotlin"
        val bookItems = listOf(VolumeItem(VolumeInfo("Programming Kotlin", listOf("Author"), 200, ImageLinks("url"))))
        val response = Response.success(BookSearchResponse(bookItems))
        coEvery { mockApiService.searchBooks(query) } returns response

        initializeViewModel()
        viewModel.searchBooks(query)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.searchResults.value shouldBe bookItems
        viewModel.isSearching.value shouldBe false
    }

    @Test
    @DisplayName("La búsqueda de libros con query vacía limpia los resultados")
    fun `búsqueda con query vacía limpia resultados`() = runTest {
        initializeViewModel()
        viewModel.searchBooks("")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.searchResults.value.isEmpty() shouldBe true
    }

    @Test
    @DisplayName("La búsqueda de libros falla y limpia los resultados")
    fun `la búsqueda falla y limpia los resultados`() = runTest {
        val query = "Kotlin"
        coEvery { mockApiService.searchBooks(query) } throws Exception("Network error")

        initializeViewModel()
        viewModel.searchBooks(query)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.searchResults.value.isEmpty() shouldBe true
        viewModel.isSearching.value shouldBe false
    }

    @Test
    @DisplayName("Al seleccionar un libro, el UI state se actualiza")
    fun `seleccionar libro actualiza ui state`() {
        val bookItem = VolumeItem(VolumeInfo("Título", listOf("Autor"), 123, ImageLinks("http://url")))
        initializeViewModel()
        viewModel.onBookSelected(bookItem)

        with(viewModel.uiState.value) {
            titulo shouldBe "Título"
            autor shouldBe "Autor"
            totalPaginas shouldBe "123"
            portada shouldBe "https://url"
        }
        viewModel.searchResults.value.isEmpty() shouldBe true
    }

    @Test
    @DisplayName("onTituloChange actualiza el título en el estado")
    fun `onTituloChange actualiza el título`() {
        initializeViewModel()
        viewModel.onTituloChange("Nuevo Título")
        viewModel.uiState.value.titulo shouldBe "Nuevo Título"
    }
    
    @Test
    @DisplayName("onAutorChange actualiza el autor en el estado")
    fun `onAutorChange actualiza el autor`() {
        initializeViewModel()
        viewModel.onAutorChange("Nuevo Autor")
        viewModel.uiState.value.autor shouldBe "Nuevo Autor"
    }

    @Test
    @DisplayName("onTotalPaginasChange actualiza las páginas en el estado")
    fun `onTotalPaginasChange actualiza las páginas`() {
        initializeViewModel()
        viewModel.onTotalPaginasChange("300")
        viewModel.uiState.value.totalPaginas shouldBe "300"
        viewModel.onTotalPaginasChange("abc") // No debería cambiar
        viewModel.uiState.value.totalPaginas shouldBe "300"
    }

    @Test
    @DisplayName("guardarLibro con campos inválidos muestra errores")
    fun `guardarLibro con campos inválidos`() {
        initializeViewModel()
        viewModel.guardarLibro()
        with(viewModel.uiState.value) {
            errorTitulo shouldBe "El título no puede estar vacío"
        }

        viewModel.onTituloChange("Título Válido")
        viewModel.guardarLibro()
        with(viewModel.uiState.value) {
            errorAutor shouldBe "El autor no puede estar vacío"
        }

        viewModel.onAutorChange("Autor Válido")
        viewModel.guardarLibro()
        with(viewModel.uiState.value) {
            errorTotalPaginas shouldBe "Número de páginas inválido"
        }
    }

    @Test
    @DisplayName("guardarLibro sin usuario muestra error")
    fun `guardarLibro sin usuario`() = runTest {
        coEvery { preferenciasRepository.usuarioEmailFlow } returns flowOf(null)
        coEvery { usuarioRepository.getUsuarioPorEmail(any()) } returns null

        initializeViewModel()
        val job = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect{ } }
        viewModel.onTituloChange("Título")
        viewModel.onAutorChange("Autor")
        viewModel.onTotalPaginasChange("100")

        viewModel.guardarLibro()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.errorGeneral shouldBe "Error: No se pudo encontrar el usuario actual"
        job.cancel()
    }

    @Test
    @DisplayName("guardarLibro con éxito para un libro nuevo")
    fun `guardarLibro con éxito para libro nuevo`() = runTest {
        val email = "test@user.com"
        val usuario = Usuario(1, "Test User", email, "", LocalDate.now())
        coEvery { preferenciasRepository.usuarioEmailFlow } returns flowOf(email)
        coEvery { usuarioRepository.getUsuarioPorEmail(email) } returns usuario

        initializeViewModel()
        viewModel.onTituloChange("Título")
        viewModel.onAutorChange("Autor")
        viewModel.onTotalPaginasChange("100")

        viewModel.guardarLibro()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.libroGuardado shouldBe true
        coVerify { libroRepository.agregarLibro(any()) }
    }

    @Test
    @DisplayName("guardarLibro en modo edición actualiza el libro")
    fun `guardarLibro en modo edición`() = runTest {
        val libroId = 123
        val libro = Libro(libroId, 1L, "Título Viejo", "Autor Viejo", "", 100, 50, "leyendo")
        savedStateHandle = SavedStateHandle(mapOf("libroId" to libroId))
        coEvery { libroRepository.obtenerLibroPorId(libroId) } returns flowOf(libro)

        val email = "test@user.com"
        val usuario = Usuario(1, "Test User", email, "", LocalDate.now())
        coEvery { preferenciasRepository.usuarioEmailFlow } returns flowOf(email)
        coEvery { usuarioRepository.getUsuarioPorEmail(email) } returns usuario

        initializeViewModel()
        testDispatcher.scheduler.advanceUntilIdle() // Permite que se cargue el libro a editar

        viewModel.onTituloChange("Título Nuevo")
        viewModel.guardarLibro()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.libroGuardado shouldBe true
        coVerify { libroRepository.actualizarLibro(any()) }
    }
}
