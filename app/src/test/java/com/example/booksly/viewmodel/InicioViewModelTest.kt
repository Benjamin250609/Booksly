
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
import kotlinx.coroutines.flow.first
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
class InicioViewModelTest {

    private val libroRepository: LibroRepository = mockk()
    private val usuarioRepository: UsuarioRepository = mockk()
    private val preferenciasRepository: PreferenciasRepository = mockk()
    private lateinit var viewModel: InicioViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val userEmailFlow = MutableStateFlow<String?>(null)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { preferenciasRepository.usuarioEmailFlow } returns userEmailFlow
    }

    private fun initializeViewModel() {
        viewModel = InicioViewModel(libroRepository, usuarioRepository, preferenciasRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("Cuando no hay email de usuario, la lista de libros debe estar vacía")
    fun `sin email de usuario, la lista de libros está vacía`() = runTest {
        // Given
        userEmailFlow.value = null

        // When
        initializeViewModel()

        // Then
        val libros = viewModel.libros.first()
        libros shouldBe emptyList()
        coVerify(exactly = 0) { usuarioRepository.getUsuarioPorEmailFlow(any()) }
        coVerify(exactly = 0) { libroRepository.obtenerTodosLosLibros(any()) }
    }

    @Test
    @DisplayName("Cuando el flujo de usuario emite null, la lista de libros debe estar vacía")
    fun `usuario nulo, la lista de libros está vacía`() = runTest {
        // Given
        val email = "user@test.com"
        coEvery { usuarioRepository.getUsuarioPorEmailFlow(email) } returns flowOf(null)

        // When
        initializeViewModel()
        val job = launch(UnconfinedTestDispatcher()) { viewModel.libros.collect() }
        userEmailFlow.value = email

        // Then
        testDispatcher.scheduler.advanceUntilIdle()
        val libros = viewModel.libros.value
        libros shouldBe emptyList()
        coVerify(exactly = 1) { usuarioRepository.getUsuarioPorEmailFlow(email) }
        coVerify(exactly = 0) { libroRepository.obtenerTodosLosLibros(any()) }
        job.cancel()
    }

    @Test
    @DisplayName("Cuando hay un usuario pero no tiene libros, la lista de libros debe estar vacía")
    fun `usuario sin libros, la lista está vacía`() = runTest {
        // Given
        val email = "user@test.com"
        val userId = 1L
        val usuario = Usuario(userId.toInt(), "Test User", email, "", LocalDate.now())

        coEvery { usuarioRepository.getUsuarioPorEmailFlow(email) } returns flowOf(usuario)
        coEvery { libroRepository.obtenerTodosLosLibros(userId) } returns flowOf(emptyList())

        // When
        initializeViewModel()
        val job = launch(UnconfinedTestDispatcher()) { viewModel.libros.collect() }
        userEmailFlow.value = email

        // Then
        testDispatcher.scheduler.advanceUntilIdle()
        val libros = viewModel.libros.value
        libros shouldBe emptyList()
        coVerify(exactly = 1) { libroRepository.obtenerTodosLosLibros(userId) }
        job.cancel()
    }

    @Test
    @DisplayName("Cuando hay un usuario con libros, la lista de libros se debe poblar correctamente")
    fun `usuario con libros, la lista se puebla`() = runTest {
        // Given
        val email = "user@test.com"
        val userId = 1L
        val usuario = Usuario(userId.toInt(), "Test User", email, "", LocalDate.now())
        val listaLibros = listOf(
            Libro(1, userId, "El Señor de los Anillos", "J.R.R. Tolkien", "", 1200, 100, "Leyendo"),
            Libro(2, userId, "1984", "George Orwell", "", 300, 300, "Leído")
        )

        coEvery { usuarioRepository.getUsuarioPorEmailFlow(email) } returns flowOf(usuario)
        coEvery { libroRepository.obtenerTodosLosLibros(userId) } returns flowOf(listaLibros)

        // When
        initializeViewModel()
        val job = launch(UnconfinedTestDispatcher()) { viewModel.libros.collect() }
        userEmailFlow.value = email

        // Then
        testDispatcher.scheduler.advanceUntilIdle()
        val libros = viewModel.libros.value
        libros shouldBe listaLibros
        libros.size shouldBe 2
        job.cancel()
    }

    @Test
    @DisplayName("Cuando el usuario cierra sesión, la lista de libros debe vaciarse")
    fun `al cerrar sesión, la lista de libros se vacía`() = runTest {
        // Given - First user is logged in
        val email = "user@test.com"
        val userId = 1L
        val usuario = Usuario(userId.toInt(), "Test User", email, "", LocalDate.now())
        val listaLibros = listOf(Libro(1, userId, "Título", "Autor", "", 100, 10, "Leyendo"))

        coEvery { usuarioRepository.getUsuarioPorEmailFlow(email) } returns flowOf(usuario)
        coEvery { libroRepository.obtenerTodosLosLibros(userId) } returns flowOf(listaLibros)

        initializeViewModel()
        val job = launch(UnconfinedTestDispatcher()) { viewModel.libros.collect() } 
        userEmailFlow.value = email
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.libros.value shouldBe listaLibros

        // When - User logs out
        coEvery { usuarioRepository.getUsuarioPorEmailFlow(email) } returns flowOf(null)
        userEmailFlow.value = null

        // Then
        testDispatcher.scheduler.advanceUntilIdle()
        val libros = viewModel.libros.value
        libros shouldBe emptyList()
        job.cancel()
    }
}
