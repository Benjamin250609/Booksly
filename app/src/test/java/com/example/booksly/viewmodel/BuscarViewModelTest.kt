
package com.example.booksly.viewmodel

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
import kotlinx.coroutines.flow.MutableStateFlow
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
import java.time.LocalDate

@ExperimentalCoroutinesApi
class BuscarViewModelTest {

    private val libroRepository: LibroRepository = mockk(relaxed = true)
    private val usuarioRepository: UsuarioRepository = mockk(relaxed = true)
    private val preferenciasRepository: PreferenciasRepository = mockk(relaxed = true)
    private lateinit var viewModel: BuscarViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val userEmailFlow = MutableStateFlow<String?>(null)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { preferenciasRepository.usuarioEmailFlow } returns userEmailFlow
    }

    private fun initializeViewModel() {
        viewModel = BuscarViewModel(libroRepository, usuarioRepository, preferenciasRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("Estado inicial debe ser vacío")
    fun `estado inicial vacío`() = runTest {
        initializeViewModel()
        viewModel.uiState.value shouldBe BuscarUiState()
    }

    @Test
    @DisplayName("Búsqueda con usuario logueado y resultados")
    fun `búsqueda con usuario y resultados`() = runTest {
        // Given
        val email = "user@test.com"
        val userId = 1L
        val usuario = Usuario(userId.toInt(), "Test User", email, "", LocalDate.now())
        val termino = "Anillos"
        val listaLibros = listOf(Libro(1, userId, "El Señor de los Anillos", "J.R.R. Tolkien", "", 1200, 100, "Leyendo"))

        userEmailFlow.value = email
        coEvery { usuarioRepository.getUsuarioPorEmailFlow(email) } returns flowOf(usuario)
        coEvery { libroRepository.buscarLibrosPorTermino(userId, termino) } returns flowOf(listaLibros)

        // When
        initializeViewModel()
        val job = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        viewModel.onTerminoBusquedaChange(termino)

        // Then
        testDispatcher.scheduler.advanceTimeBy(301) // Avanza el tiempo para pasar el debounce

        with(viewModel.uiState.value) {
            terminoBusqueda shouldBe termino
            resultados shouldBe listaLibros
            sinResultados shouldBe false
        }
        job.cancel()
    }

    @Test
    @DisplayName("Búsqueda con usuario logueado sin resultados")
    fun `búsqueda con usuario sin resultados`() = runTest {
        // Given
        val email = "user@test.com"
        val userId = 1L
        val usuario = Usuario(userId.toInt(), "Test User", email, "", LocalDate.now())
        val termino = "Matrix"

        userEmailFlow.value = email
        coEvery { usuarioRepository.getUsuarioPorEmailFlow(email) } returns flowOf(usuario)
        coEvery { libroRepository.buscarLibrosPorTermino(userId, termino) } returns flowOf(emptyList())

        // When
        initializeViewModel()
        val job = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        viewModel.onTerminoBusquedaChange(termino)

        // Then
        testDispatcher.scheduler.advanceTimeBy(301)

        with(viewModel.uiState.value) {
            terminoBusqueda shouldBe termino
            resultados shouldBe emptyList()
            sinResultados shouldBe true
        }
        job.cancel()
    }

    @Test
    @DisplayName("Al borrar el término de búsqueda, los resultados se limpian")
    fun `borrar término limpia resultados`() = runTest {
        // Given: Hacemos una búsqueda inicial
        val email = "user@test.com"
        val userId = 1L
        val usuario = Usuario(userId.toInt(), "Test User", email, "", LocalDate.now())
        val termino = "Anillos"
        val listaLibros = listOf(Libro(1, userId, "El Señor de los Anillos", "J.R.R. Tolkien", "", 1200, 100, "Leyendo"))

        userEmailFlow.value = email
        coEvery { usuarioRepository.getUsuarioPorEmailFlow(email) } returns flowOf(usuario)
        coEvery { libroRepository.buscarLibrosPorTermino(userId, termino) } returns flowOf(listaLibros)

        initializeViewModel()
        val job = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        viewModel.onTerminoBusquedaChange(termino)
        testDispatcher.scheduler.advanceTimeBy(301)
        viewModel.uiState.value.resultados shouldBe listaLibros

        // When: Borramos el término
        viewModel.onTerminoBusquedaChange("")
        testDispatcher.scheduler.advanceTimeBy(301)

        // Then
        with(viewModel.uiState.value) {
            terminoBusqueda shouldBe ""
            resultados shouldBe emptyList()
            sinResultados shouldBe false
        }
        job.cancel()
    }

    @Test
    @DisplayName("La búsqueda no se ejecuta si el usuario no está logueado")
    fun `búsqueda sin usuario no se ejecuta`() = runTest {
        // Given
        val termino = "buscar algo"
        userEmailFlow.value = null
        coEvery { usuarioRepository.getUsuarioPorEmailFlow(any()) } returns flowOf(null)

        // When
        initializeViewModel()
        val job = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        viewModel.onTerminoBusquedaChange(termino)
        testDispatcher.scheduler.advanceTimeBy(301)

        // Then
        with(viewModel.uiState.value) {
            resultados shouldBe emptyList()
            sinResultados shouldBe true
        }
        coVerify(exactly = 0) { libroRepository.buscarLibrosPorTermino(any(), any()) }
        job.cancel()
    }

    @Test
    @DisplayName("Si el usuario cambia, la búsqueda se actualiza")
    fun `cambio de usuario actualiza la búsqueda`() = runTest {
        // Given: User 1 está logueado
        val email1 = "user1@test.com"
        val userId1 = 1L
        val usuario1 = Usuario(userId1.toInt(), "User One", email1, "", LocalDate.now())
        val termino = "Libro"
        val librosUser1 = listOf(Libro(1, userId1, "Libro de User1", "Autor1", "", 100, 1, "Leyendo"))

        coEvery { usuarioRepository.getUsuarioPorEmailFlow(email1) } returns flowOf(usuario1)
        coEvery { libroRepository.buscarLibrosPorTermino(userId1, termino) } returns flowOf(librosUser1)

        initializeViewModel()
        val job = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        userEmailFlow.value = email1
        viewModel.onTerminoBusquedaChange(termino)
        testDispatcher.scheduler.advanceTimeBy(301)
        viewModel.uiState.value.resultados shouldBe librosUser1

        // When: User 2 inicia sesión
        val email2 = "user2@test.com"
        val userId2 = 2L
        val usuario2 = Usuario(userId2.toInt(), "User Two", email2, "", LocalDate.now())
        val librosUser2 = listOf(Libro(2, userId2, "Libro de User2", "Autor2", "", 200, 2, "Leído"))

        coEvery { usuarioRepository.getUsuarioPorEmailFlow(email2) } returns flowOf(usuario2)
        coEvery { libroRepository.buscarLibrosPorTermino(userId2, termino) } returns flowOf(librosUser2)
        userEmailFlow.value = email2
        testDispatcher.scheduler.advanceTimeBy(301)

        // Then
        viewModel.uiState.value.resultados shouldBe librosUser2
        job.cancel()
    }
}
